let allPorts = [];
let allEquipment = [];
let currentPortId = null;
let sourcePortIdForConnect = null;

// Загрузка данных при старте
document.addEventListener('DOMContentLoaded', () => {
    loadEquipment();
    loadPorts();
});

// Загрузка списка оборудования
async function loadEquipment() {
    try {
        allEquipment = await api.get('/equipment');
        populateEquipmentSelect();
        populateEquipmentFilter();
        populateConnectedEquipmentSelect();
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

// Заполнение select для подключения
function populateConnectedEquipmentSelect() {
    const select = document.getElementById('connectedToEquipmentId');
    select.innerHTML = '<option value="">Не подключено</option>' +
        allEquipment.map(eq =>
            `<option value="${eq.id}">${escapeHtml(eq.name)}</option>`
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

// Загрузка всех портов
async function loadPorts() {
    try {
        allPorts = await api.get('/device-ports');
        displayPorts(allPorts);
        updateStats();
        populateTypeFilter();
    } catch (error) {
        formUtils.showError('Ошибка загрузки портов: ' + error.message);
        document.getElementById('portsBody').innerHTML =
            '<tr><td colspan="9" class="error">Ошибка загрузки данных</td></tr>';
    }
}

// Отображение портов в таблице
function displayPorts(ports) {
    const tbody = document.getElementById('portsBody');

    if (ports.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" class="no-data">Нет данных</td></tr>';
        document.getElementById('displayedCount').textContent = '0';
        return;
    }

    tbody.innerHTML = ports.map(port => `
        <tr>
            <td>${port.id}</td>
            <td>${port.equipmentName ? escapeHtml(port.equipmentName) : '<span class="text-muted">N/A</span>'}</td>
            <td><strong>Port ${port.portNumber}</strong></td>
            <td>${port.portType ? `<span class="badge badge-info">${escapeHtml(port.portType)}</span>` : '<span class="text-muted">-</span>'}</td>
            <td>${getStatusBadge(port.status)}</td>
            <td>${port.speed ? escapeHtml(port.speed) : '<span class="text-muted">-</span>'}</td>
            <td>${getConnectionInfo(port)}</td>
            <td>${port.description ? escapeHtml(port.description) : '<span class="text-muted">-</span>'}</td>
            <td>
                <button class="btn btn-info btn-small" onclick="viewDetails(${port.id})" title="Просмотр">👁️</button>
                ${!port.connectedToPortId ? `<button class="btn btn-success btn-small" onclick="openConnectModal(${port.id})" title="Подключить">🔗</button>` : ''}
                ${port.connectedToPortId ? `<button class="btn btn-warning btn-small" onclick="disconnectPort(${port.id})" title="Отключить">🔌</button>` : ''}
                <button class="btn btn-primary btn-small" onclick="editPort(${port.id})" title="Редактировать">✏️</button>
                <button class="btn btn-danger btn-small" onclick="deletePort(${port.id})" title="Удалить">🗑️</button>
            </td>
        </tr>
    `).join('');

    document.getElementById('displayedCount').textContent = ports.length;
}

// Получение бейджа статуса
function getStatusBadge(status) {
    const badges = {
        'Active': '<span class="badge badge-success">Active</span>',
        'Inactive': '<span class="badge badge-secondary">Inactive</span>',
        'Reserved': '<span class="badge badge-warning">Reserved</span>'
    };
    return badges[status] || `<span class="badge badge-secondary">${escapeHtml(status)}</span>`;
}

// Получение информации о подключении
function getConnectionInfo(port) {
    if (port.connectedToEquipmentName && port.connectedToPortNumber) {
        return `${escapeHtml(port.connectedToEquipmentName)} → Port ${port.connectedToPortNumber}`;
    }
    return '<span class="text-muted">Не подключено</span>';
}

// Фильтрация таблицы
function filterTable() {
    const searchText = document.getElementById('searchInput').value.toLowerCase().trim();
    const equipmentFilter = document.getElementById('equipmentFilter').value;
    const statusFilter = document.getElementById('statusFilter').value;
    const typeFilter = document.getElementById('typeFilter').value;
    const connectionFilter = document.getElementById('connectionFilter').value;

    const filtered = allPorts.filter(port => {
        // Поиск по тексту
        const matchSearch = !searchText ||
            (port.equipmentName && port.equipmentName.toLowerCase().includes(searchText)) ||
            (port.description && port.description.toLowerCase().includes(searchText)) ||
            (port.connectedToEquipmentName && port.connectedToEquipmentName.toLowerCase().includes(searchText));

        // Фильтр по оборудованию
        const matchEquipment = !equipmentFilter || port.equipmentId == equipmentFilter;

        // Фильтр по статусу
        const matchStatus = !statusFilter || port.status === statusFilter;

        // Фильтр по типу
        const matchType = !typeFilter || port.portType === typeFilter;

        // Фильтр по подключению
        const matchConnection = !connectionFilter ||
            (connectionFilter === 'connected' && port.connectedToPortId) ||
            (connectionFilter === 'available' && !port.connectedToPortId);

        return matchSearch && matchEquipment && matchStatus && matchType && matchConnection;
    });

    displayPorts(filtered);
}

// Сброс фильтров
function resetFilters() {
    document.getElementById('searchInput').value = '';
    document.getElementById('equipmentFilter').value = '';
    document.getElementById('statusFilter').value = '';
    document.getElementById('typeFilter').value = '';
    document.getElementById('connectionFilter').value = '';
    displayPorts(allPorts);
}

// Обновление статистики
function updateStats() {
    const activeCount = allPorts.filter(p => p.status === 'Active').length;
    const connectedCount = allPorts.filter(p => p.connectedToPortId).length;

    document.getElementById('totalCount').textContent = allPorts.length;
    document.getElementById('activeCount').textContent = activeCount;
    document.getElementById('connectedCount').textContent = connectedCount;
    document.getElementById('displayedCount').textContent = allPorts.length;
}

// Заполнение фильтра типов портов
function populateTypeFilter() {
    const types = [...new Set(allPorts
        .map(p => p.portType)
        .filter(t => t)
    )].sort();

    const select = document.getElementById('typeFilter');
    const currentValue = select.value;

    select.innerHTML = '<option value="">Все типы</option>' +
        types.map(type => `<option value="${escapeHtml(type)}">${escapeHtml(type)}</option>`).join('');

    select.value = currentValue;
}

// Загрузка портов для целевого оборудования (в форме)
async function loadTargetPorts() {
    const equipmentId = document.getElementById('connectedToEquipmentId').value;
    const select = document.getElementById('connectedToPortId');

    if (!equipmentId) {
        select.innerHTML = '<option value="">Сначала выберите оборудование</option>';
        select.disabled = true;
        return;
    }

    try {
        const ports = await api.get(`/device-ports/equipment/${equipmentId}/available`);
        select.innerHTML = '<option value="">Выберите порт...</option>' +
            ports.map(p =>
                `<option value="${p.id}">Port ${p.portNumber} ${p.portType ? '(' + escapeHtml(p.portType) + ')' : ''}</option>`
            ).join('');
        select.disabled = false;
    } catch (error) {
        console.error('Ошибка загрузки портов:', error);
        select.innerHTML = '<option value="">Ошибка загрузки</option>';
        select.disabled = true;
    }
}

// Открытие модального окна для добавления
function openModal() {
    document.getElementById('modalTitle').textContent = 'Добавить порт';
    document.getElementById('portForm').reset();
    document.getElementById('portId').value = '';
    document.getElementById('status').value = 'Inactive';
    document.getElementById('connectedToPortId').disabled = true;

    document.getElementById('modal').classList.add('active');
    document.getElementById('equipmentId').focus();
}

// Закрытие модального окна
function closeModal() {
    document.getElementById('modal').classList.remove('active');
    document.getElementById('portForm').reset();
}

// Просмотр деталей порта
async function viewDetails(id) {
    try {
        const port = await api.get(`/device-ports/${id}`);
        currentPortId = id;

        document.getElementById('detailId').textContent = port.id;
        document.getElementById('detailEquipmentName').textContent = port.equipmentName || 'N/A';
        document.getElementById('detailPortNumber').textContent = 'Port ' + port.portNumber;
        document.getElementById('detailPortType').textContent = port.portType || 'Не указан';
        document.getElementById('detailStatus').innerHTML = getStatusBadge(port.status);
        document.getElementById('detailSpeed').textContent = port.speed || 'Не указана';
        document.getElementById('detailConnection').innerHTML = getConnectionInfo(port);
        document.getElementById('detailDescription').textContent = port.description || 'Нет описания';

        document.getElementById('detailsModal').classList.add('active');
    } catch (error) {
        formUtils.showError('Ошибка загрузки данных порта: ' + error.message);
    }
}

// Закрытие модального окна деталей
function closeDetailsModal() {
    document.getElementById('detailsModal').classList.remove('active');
    currentPortId = null;
}

// Редактирование из модального окна деталей
function editFromDetails() {
    closeDetailsModal();
    if (currentPortId) {
        editPort(currentPortId);
    }
}

// Отключение из модального окна деталей
function disconnectFromDetails() {
    if (currentPortId) {
        disconnectPort(currentPortId);
        closeDetailsModal();
    }
}

// Редактирование порта
async function editPort(id) {
    try {
        const port = await api.get(`/device-ports/${id}`);

        document.getElementById('modalTitle').textContent = 'Редактировать порт';
        document.getElementById('portId').value = port.id;
        document.getElementById('equipmentId').value = port.equipmentId;
        document.getElementById('portNumber').value = port.portNumber;
        document.getElementById('portType').value = port.portType || '';
        document.getElementById('status').value = port.status;
        document.getElementById('speed').value = port.speed || '';
        document.getElementById('description').value = port.description || '';
        document.getElementById('connectedToEquipmentId').value = port.connectedToEquipmentId || '';

        if (port.connectedToEquipmentId) {
            await loadTargetPorts();
            document.getElementById('connectedToPortId').value = port.connectedToPortId || '';
        }

        document.getElementById('modal').classList.add('active');
        document.getElementById('portNumber').focus();
    } catch (error) {
        formUtils.showError('Ошибка загрузки данных порта: ' + error.message);
    }
}

// Сохранение порта (создание или обновление)
async function savePort(event) {
    event.preventDefault();

    const id = document.getElementById('portId').value;
    const equipmentId = parseInt(document.getElementById('equipmentId').value);
    const portNumber = parseInt(document.getElementById('portNumber').value);

    const data = {
        equipmentId: equipmentId,
        portNumber: portNumber,
        portType: document.getElementById('portType').value.trim() || null,
        status: document.getElementById('status').value,
        speed: document.getElementById('speed').value.trim() || null,
        connectedToEquipmentId: document.getElementById('connectedToEquipmentId').value ?
            parseInt(document.getElementById('connectedToEquipmentId').value) : null,
        connectedToPortId: document.getElementById('connectedToPortId').value ?
            parseInt(document.getElementById('connectedToPortId').value) : null,
        description: document.getElementById('description').value.trim() || null
    };

    try {
        if (id) {
            // Обновление
            await api.put(`/device-ports/${id}`, data);
            formUtils.showSuccess('✅ Порт успешно обновлён');
        } else {
            // Создание
            await api.post('/device-ports', data);
            formUtils.showSuccess('✅ Порт успешно создан');
        }

        closeModal();
        await loadPorts();
    } catch (error) {
        formUtils.showError('❌ Ошибка сохранения: ' + error.message);
    }
}

// Открытие модального окна подключения
async function openConnectModal(portId) {
    sourcePortIdForConnect = portId;

    const port = allPorts.find(p => p.id === portId);
    if (!port) return;

    document.getElementById('sourcePortInfo').textContent =
        `${port.equipmentName} → Port ${port.portNumber}`;

    // Заполнение оборудования (исключая текущее)
    const select = document.getElementById('targetEquipmentSelect');
    select.innerHTML = '<option value="">Выберите оборудование...</option>' +
        allEquipment
            .filter(eq => eq.id !== port.equipmentId)
            .map(eq => `<option value="${eq.id}">${escapeHtml(eq.name)}</option>`)
            .join('');

    document.getElementById('targetPortSelect').innerHTML =
        '<option value="">Сначала выберите оборудование</option>';
    document.getElementById('targetPortSelect').disabled = true;

    document.getElementById('connectModal').classList.add('active');
}

// Закрытие модального окна подключения
function closeConnectModal() {
    document.getElementById('connectModal').classList.remove('active');
    sourcePortIdForConnect = null;
}

// Загрузка портов для подключения
async function loadTargetPortsForConnect() {
    const equipmentId = document.getElementById('targetEquipmentSelect').value;
    const select = document.getElementById('targetPortSelect');

    if (!equipmentId) {
        select.innerHTML = '<option value="">Сначала выберите оборудование</option>';
        select.disabled = true;
        return;
    }

    try {
        const ports = await api.get(`/device-ports/equipment/${equipmentId}/available`);
        select.innerHTML = '<option value="">Выберите порт...</option>' +
            ports.map(p =>
                `<option value="${p.id}">Port ${p.portNumber} ${p.portType ? '(' + escapeHtml(p.portType) + ')' : ''} - ${p.status}</option>`
            ).join('');
        select.disabled = false;
    } catch (error) {
        console.error('Ошибка загрузки портов:', error);
        select.innerHTML = '<option value="">Ошибка загрузки</option>';
        select.disabled = true;
    }
}

// Подтверждение подключения
async function confirmConnect() {
    const targetPortId = document.getElementById('targetPortSelect').value;

    if (!targetPortId) {
        formUtils.showError('Выберите порт назначения');
        return;
    }

    try {
        await api.patch(`/device-ports/${sourcePortIdForConnect}/connect/${targetPortId}`);
        formUtils.showSuccess('✅ Порты успешно подключены');
        closeConnectModal();
        await loadPorts();
    } catch (error) {
        formUtils.showError('❌ Ошибка подключения: ' + error.message);
    }
}

// Отключение порта
async function disconnectPort(id) {
    if (!formUtils.confirm('Вы уверены, что хотите отключить этот порт?')) {
        return;
    }

    try {
        await api.patch(`/device-ports/${id}/disconnect`);
        formUtils.showSuccess('✅ Порт успешно отключён');
        await loadPorts();
    } catch (error) {
        formUtils.showError('❌ Ошибка отключения: ' + error.message);
    }
}

// Удаление порта
async function deletePort(id) {
    const port = allPorts.find(p => p.id === id);
    if (!port) return;

    const confirmMessage = `Вы уверены, что хотите удалить порт ${port.portNumber} оборудования "${port.equipmentName}"?`;

    if (!formUtils.confirm(confirmMessage)) {
        return;
    }

    try {
        await api.delete(`/device-ports/${id}`);
        formUtils.showSuccess('✅ Порт успешно удалён');
        await loadPorts();
    } catch (error) {
        formUtils.showError('❌ Ошибка удаления: ' + error.message);
    }
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

// Обработка нажатия Enter в поле поиска
document.getElementById('searchInput').addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        filterTable();
    }
});

// Закрытие модальных окон по клику вне их
window.addEventListener('click', function(event) {
    const modal = document.getElementById('modal');
    const detailsModal = document.getElementById('detailsModal');
    const connectModal = document.getElementById('connectModal');

    if (event.target === modal) {
        closeModal();
    }
    if (event.target === detailsModal) {
        closeDetailsModal();
    }
    if (event.target === connectModal) {
        closeConnectModal();
    }
});

// Закрытие по Escape
document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape') {
        closeModal();
        closeDetailsModal();
        closeConnectModal();
    }
});
