let allIpAddresses = [];
let allEquipment = [];
let currentIpAddressId = null;

// Загрузка данных при старте
document.addEventListener('DOMContentLoaded', () => {
    loadEquipment();
    loadIpAddresses();
});

// Загрузка списка оборудования
async function loadEquipment() {
    try {
        allEquipment = await api.get('/equipment');
        populateEquipmentSelect();
        populateEquipmentFilter();
    } catch (error) {
        console.error('Ошибка загрузки оборудования:', error);
        formUtils.showError('Ошибка загрузки списка оборудования');
    }
}

// Заполнение select оборудования в форме
function populateEquipmentSelect() {
    const select = document.getElementById('equipmentId');
    select.innerHTML = '<option value="">Выберите оборудование...</option>' +
        allEquipment.map(eq =>
            `<option value="${eq.id}">${escapeHtml(eq.name)} (ID: ${eq.id})</option>`
        ).join('');
}

// Заполнение фильтра оборудования
function populateEquipmentFilter() {
    const select = document.getElementById('equipmentFilter');
    const currentValue = select.value;

    select.innerHTML = '<option value="">Всё оборудование</option>' +
        allEquipment.map(eq =>
            `<option value="${eq.id}">${escapeHtml(eq.name)}</option>`
        ).join('');

    select.value = currentValue;
}

// Загрузка всех IP-адресов
async function loadIpAddresses() {
    try {
        allIpAddresses = await api.get('/ip-addresses');
        displayIpAddresses(allIpAddresses);
        updateStats();
        populateNetworkTypeFilter();
    } catch (error) {
        formUtils.showError('Ошибка загрузки IP-адресов: ' + error.message);
        document.getElementById('ipAddressesBody').innerHTML =
            '<tr><td colspan="9" class="error">Ошибка загрузки данных</td></tr>';
    }
}

// Отображение IP-адресов в таблице
function displayIpAddresses(ipAddresses) {
    const tbody = document.getElementById('ipAddressesBody');

    if (ipAddresses.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" class="no-data">Нет данных</td></tr>';
        document.getElementById('displayedCount').textContent = '0';
        return;
    }

    tbody.innerHTML = ipAddresses.map(ip => `
        <tr>
            <td>${ip.id}</td>
            <td><strong>${escapeHtml(ip.ipAddress)}</strong></td>
            <td>${ip.equipmentName ? escapeHtml(ip.equipmentName) : '<span class="text-muted">N/A</span>'}</td>
            <td>${ip.subnetMask ? escapeHtml(ip.subnetMask) : '<span class="text-muted">-</span>'}</td>
            <td>${ip.gateway ? escapeHtml(ip.gateway) : '<span class="text-muted">-</span>'}</td>
            <td>${ip.networkType ? `<span class="badge badge-info">${escapeHtml(ip.networkType)}</span>` : '<span class="text-muted">-</span>'}</td>
            <td>${ip.isPrimary ? '<span class="badge badge-success">✓ Основной</span>' : '<span class="badge badge-secondary">Дополнительный</span>'}</td>
            <td>${ip.assignedDate ? formatDate(ip.assignedDate) : '<span class="text-muted">-</span>'}</td>
            <td>
                <button class="btn btn-info btn-small" onclick="viewDetails(${ip.id})" title="Просмотр">👁️</button>
                ${!ip.isPrimary ? `<button class="btn btn-warning btn-small" onclick="setPrimary(${ip.id})" title="Сделать основным">⭐</button>` : ''}
                <button class="btn btn-primary btn-small" onclick="editIpAddress(${ip.id})" title="Редактировать">✏️</button>
                <button class="btn btn-danger btn-small" onclick="deleteIpAddress(${ip.id})" title="Удалить">🗑️</button>
            </td>
        </tr>
    `).join('');

    document.getElementById('displayedCount').textContent = ipAddresses.length;
}

// Фильтрация таблицы
function filterTable() {
    const searchText = document.getElementById('searchInput').value.toLowerCase().trim();
    const equipmentFilter = document.getElementById('equipmentFilter').value;
    const networkTypeFilter = document.getElementById('networkTypeFilter').value;
    const primaryFilter = document.getElementById('primaryFilter').value;

    const filtered = allIpAddresses.filter(ip => {
        // Поиск по тексту
        const matchSearch = !searchText ||
            ip.ipAddress.toLowerCase().includes(searchText) ||
            (ip.equipmentName && ip.equipmentName.toLowerCase().includes(searchText)) ||
            (ip.gateway && ip.gateway.toLowerCase().includes(searchText)) ||
            (ip.subnetMask && ip.subnetMask.toLowerCase().includes(searchText));

        // Фильтр по оборудованию
        const matchEquipment = !equipmentFilter || ip.equipmentId == equipmentFilter;

        // Фильтр по типу сети
        const matchNetworkType = !networkTypeFilter || ip.networkType === networkTypeFilter;

        // Фильтр по основному IP
        const matchPrimary = !primaryFilter ||
            (primaryFilter === 'true' && ip.isPrimary) ||
            (primaryFilter === 'false' && !ip.isPrimary);

        return matchSearch && matchEquipment && matchNetworkType && matchPrimary;
    });

    displayIpAddresses(filtered);
}

// Сброс фильтров
function resetFilters() {
    document.getElementById('searchInput').value = '';
    document.getElementById('equipmentFilter').value = '';
    document.getElementById('networkTypeFilter').value = '';
    document.getElementById('primaryFilter').value = '';
    displayIpAddresses(allIpAddresses);
}

// Обновление статистики
function updateStats() {
    const primaryCount = allIpAddresses.filter(ip => ip.isPrimary).length;
    document.getElementById('totalCount').textContent = allIpAddresses.length;
    document.getElementById('primaryCount').textContent = primaryCount;
    document.getElementById('displayedCount').textContent = allIpAddresses.length;
}

// Заполнение фильтра типов сети
function populateNetworkTypeFilter() {
    const networkTypes = [...new Set(allIpAddresses
        .map(ip => ip.networkType)
        .filter(nt => nt)
    )].sort();

    const select = document.getElementById('networkTypeFilter');
    const currentValue = select.value;

    select.innerHTML = '<option value="">Все типы сети</option>' +
        networkTypes.map(nt => `<option value="${escapeHtml(nt)}">${escapeHtml(nt)}</option>`).join('');

    select.value = currentValue;
}

// Открытие модального окна для добавления
function openModal() {
    document.getElementById('modalTitle').textContent = 'Добавить IP-адрес';
    document.getElementById('ipAddressForm').reset();
    document.getElementById('ipAddressId').value = '';

    // Установка текущей даты
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('assignedDate').value = today;

    document.getElementById('modal').classList.add('active');
    document.getElementById('equipmentId').focus();
}

// Закрытие модального окна
function closeModal() {
    document.getElementById('modal').classList.remove('active');
    document.getElementById('ipAddressForm').reset();
}

// Просмотр деталей IP-адреса
async function viewDetails(id) {
    try {
        const ip = await api.get(`/ip-addresses/${id}`);
        currentIpAddressId = id;

        document.getElementById('detailId').textContent = ip.id;
        document.getElementById('detailIpAddress').textContent = ip.ipAddress;
        document.getElementById('detailEquipmentName').textContent = ip.equipmentName || 'N/A';
        document.getElementById('detailSubnetMask').textContent = ip.subnetMask || 'Не указана';
        document.getElementById('detailGateway').textContent = ip.gateway || 'Не указан';
        document.getElementById('detailNetworkType').textContent = ip.networkType || 'Не указан';
        document.getElementById('detailIsPrimary').innerHTML = ip.isPrimary ?
            '<span class="badge badge-success">✓ Основной</span>' :
            '<span class="badge badge-secondary">Дополнительный</span>';
        document.getElementById('detailAssignedDate').textContent =
            ip.assignedDate ? formatDate(ip.assignedDate) : 'Не указана';

        document.getElementById('detailsModal').classList.add('active');
    } catch (error) {
        formUtils.showError('Ошибка загрузки данных IP-адреса: ' + error.message);
    }
}

// Закрытие модального окна деталей
function closeDetailsModal() {
    document.getElementById('detailsModal').classList.remove('active');
    currentIpAddressId = null;
}

// Редактирование из модального окна деталей
function editFromDetails() {
    closeDetailsModal();
    if (currentIpAddressId) {
        editIpAddress(currentIpAddressId);
    }
}

// Редактирование IP-адреса
async function editIpAddress(id) {
    try {
        const ip = await api.get(`/ip-addresses/${id}`);

        document.getElementById('modalTitle').textContent = 'Редактировать IP-адрес';
        document.getElementById('ipAddressId').value = ip.id;
        document.getElementById('equipmentId').value = ip.equipmentId;
        document.getElementById('ipAddress').value = ip.ipAddress;
        document.getElementById('subnetMask').value = ip.subnetMask || '';
        document.getElementById('gateway').value = ip.gateway || '';
        document.getElementById('networkType').value = ip.networkType || '';
        document.getElementById('isPrimary').checked = ip.isPrimary || false;
        document.getElementById('assignedDate').value = ip.assignedDate || '';

        document.getElementById('modal').classList.add('active');
        document.getElementById('ipAddress').focus();
    } catch (error) {
        formUtils.showError('Ошибка загрузки данных IP-адреса: ' + error.message);
    }
}

// Сохранение IP-адреса (создание или обновление)
async function saveIpAddress(event) {
    event.preventDefault();

    const id = document.getElementById('ipAddressId').value;
    const ipAddressValue = document.getElementById('ipAddress').value.trim();
    const equipmentId = parseInt(document.getElementById('equipmentId').value);

    // Проверка уникальности IP-адреса
    if (!id) {
        try {
            const exists = await api.get(`/ip-addresses/exists?ip=${encodeURIComponent(ipAddressValue)}`);
            if (exists) {
                formUtils.showError('IP-адрес ' + ipAddressValue + ' уже существует в системе');
                return;
            }
        } catch (error) {
            console.error('Ошибка проверки IP-адреса:', error);
        }
    }

    const data = {
        equipmentId: equipmentId,
        ipAddress: ipAddressValue,
        subnetMask: document.getElementById('subnetMask').value.trim() || null,
        gateway: document.getElementById('gateway').value.trim() || null,
        networkType: document.getElementById('networkType').value.trim() || null,
        isPrimary: document.getElementById('isPrimary').checked,
        assignedDate: document.getElementById('assignedDate').value || null
    };

    try {
        if (id) {
            // Обновление
            await api.put(`/ip-addresses/${id}`, data);
            formUtils.showSuccess('✅ IP-адрес успешно обновлён');
        } else {
            // Создание
            await api.post('/ip-addresses', data);
            formUtils.showSuccess('✅ IP-адрес успешно создан');
        }

        closeModal();
        await loadIpAddresses();
    } catch (error) {
        formUtils.showError('❌ Ошибка сохранения: ' + error.message);
    }
}

// Установка IP как основного
async function setPrimary(id) {
    try {
        await api.patch(`/ip-addresses/${id}/set-primary`);
        formUtils.showSuccess('✅ IP-адрес установлен как основной');
        await loadIpAddresses();
    } catch (error) {
        formUtils.showError('❌ Ошибка установки основного IP: ' + error.message);
    }
}

// Удаление IP-адреса
async function deleteIpAddress(id) {
    const ip = allIpAddresses.find(i => i.id === id);
    if (!ip) return;

    const confirmMessage = `Вы уверены, что хотите удалить IP-адрес "${ip.ipAddress}"?`;

    if (!formUtils.confirm(confirmMessage)) {
        return;
    }

    try {
        await api.delete(`/ip-addresses/${id}`);
        formUtils.showSuccess('✅ IP-адрес успешно удалён');
        await loadIpAddresses();
    } catch (error) {
        formUtils.showError('❌ Ошибка удаления: ' + error.message);
    }
}

// Форматирование даты
function formatDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('ru-RU', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    });
}

// Экранирование HTML
function escapeHtml(text) {
    if (!text) return '';
    const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    };
    return String(text).replace(/[&<>"']/g, m => map[m]);
}

// Валидация IP-адреса при вводе
document.getElementById('ipAddress')?.addEventListener('blur', function() {
    const value = this.value.trim();
    if (value && !isValidIPv4(value)) {
        formUtils.showError('Некорректный формат IP-адреса');
        this.focus();
    }
});

// Проверка валидности IPv4
function isValidIPv4(ip) {
    const pattern = /^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/;
    return pattern.test(ip);
}

// Обработка нажатия Enter в поле поиска
document.getElementById('searchInput').addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        filterTable();
    }
});

// Закрытие модального окна по клику вне его
window.addEventListener('click', function(event) {
    const modal = document.getElementById('modal');
    const detailsModal = document.getElementById('detailsModal');

    if (event.target === modal) {
        closeModal();
    }
    if (event.target === detailsModal) {
        closeDetailsModal();
    }
});

// Закрытие по Escape
document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape') {
        closeModal();
        closeDetailsModal();
    }
});
