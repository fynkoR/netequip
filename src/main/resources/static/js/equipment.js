let allEquipment = [];
let equipmentTypes = [];
let employees = [];

// Загрузка данных при старте
document.addEventListener('DOMContentLoaded', async () => {
    await Promise.all([
        loadEquipmentTypes(),
        loadEmployees()
    ]);
    await loadEquipment();
});

// Загрузка всего оборудования
async function loadEquipment() {
    try {
        allEquipment = await api.get('/equipment');
        displayEquipment(allEquipment);
        updateStats();
    } catch (error) {
        formUtils.showError('Ошибка загрузки оборудования: ' + error.message);
        document.getElementById('equipmentBody').innerHTML =
            '<tr><td colspan="9" class="error">Ошибка загрузки данных</td></tr>';
    }
}

// Загрузка типов оборудования
async function loadEquipmentTypes() {
    try {
        equipmentTypes = await api.get('/equipment-types');
        populateTypeSelects();
    } catch (error) {
        console.error('Ошибка загрузки типов:', error);
        formUtils.showError('Ошибка загрузки типов оборудования');
    }
}

// Загрузка сотрудников
async function loadEmployees() {
    try {
        employees = await api.get('/employees');
        populateEmployeeSelects();
    } catch (error) {
        console.error('Ошибка загрузки сотрудников:', error);
        formUtils.showError('Ошибка загрузки списка сотрудников');
    }
}

// Заполнение select типов оборудования
function populateTypeSelects() {
    const formSelect = document.getElementById('typeId');
    const filterSelect = document.getElementById('typeFilter');

    formSelect.innerHTML = '<option value="">Выберите тип...</option>' +
        equipmentTypes.map(type =>
            `<option value="${type.id}">${escapeHtml(type.typeName)} - ${escapeHtml(type.manufacturer)} ${escapeHtml(type.model)}</option>`
        ).join('');

    filterSelect.innerHTML = '<option value="">Все типы</option>' +
        equipmentTypes.map(type =>
            `<option value="${type.id}">${escapeHtml(type.typeName)}</option>`
        ).join('');
}

// Заполнение select сотрудников
function populateEmployeeSelects() {
    const formSelect = document.getElementById('employeeId');
    const filterSelect = document.getElementById('employeeFilter');

    formSelect.innerHTML = '<option value="">Не назначен</option>' +
        employees.map(emp =>
            `<option value="${emp.id}">${escapeHtml(emp.fullName)}</option>`
        ).join('');

    filterSelect.innerHTML = '<option value="">Все ответственные</option>' +
        employees.map(emp =>
            `<option value="${emp.id}">${escapeHtml(emp.fullName)}</option>`
        ).join('');
}

// Отображение оборудования в таблице
function displayEquipment(equipment) {
    const tbody = document.getElementById('equipmentBody');

    if (equipment.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" class="no-data">Нет данных</td></tr>';
        document.getElementById('displayedCount').textContent = '0';
        return;
    }

    tbody.innerHTML = equipment.map(item => `
        <tr>
            <td>${item.id}</td>
            <td>
                <strong>${escapeHtml(item.name)}</strong><br>
                <small class="text-muted">SN: ${escapeHtml(item.serialNumber)}</small>
            </td>
            <td>
                ${item.typeName ? escapeHtml(item.typeName) : '<span class="text-muted">N/A</span>'}<br>
                <small class="text-muted">${item.manufacturer ? escapeHtml(item.manufacturer) : ''} ${item.model ? escapeHtml(item.model) : ''}</small>
            </td>
            <td>${item.ipAddress ? escapeHtml(item.ipAddress) : '<span class="text-muted">-</span>'}</td>
            <td><small>${item.macAddress ? escapeHtml(item.macAddress) : '<span class="text-muted">-</span>'}</small></td>
            <td>${item.employeeFullName ? escapeHtml(item.employeeFullName) : '<span class="text-muted">Не назначен</span>'}</td>
            <td>${getStatusBadge(item.status)}</td>
            <td>${item.address ? truncateText(escapeHtml(item.address), 30) : '<span class="text-muted">-</span>'}</td>
            <td>
                <button class="btn btn-info btn-small" onclick="viewEquipment(${item.id})" title="Просмотр">👁️</button>
                <button class="btn btn-primary btn-small" onclick="editEquipment(${item.id})" title="Редактировать">✏️</button>
                <button class="btn btn-danger btn-small" onclick="deleteEquipment(${item.id})" title="Удалить">🗑️</button>
            </td>
        </tr>
    `).join('');

    document.getElementById('displayedCount').textContent = equipment.length;
}

// Получение бейджа статуса
function getStatusBadge(status) {
    const badges = {
        'Active': '<span class="badge badge-success">Active</span>',
        'Inactive': '<span class="badge badge-secondary">Inactive</span>',
        'Maintenance': '<span class="badge badge-warning">Maintenance</span>',
        'Retired': '<span class="badge badge-danger">Retired</span>'
    };
    return badges[status] || `<span class="badge badge-secondary">${escapeHtml(status)}</span>`;
}

// Фильтрация таблицы
function filterTable() {
    const searchText = document.getElementById('searchInput').value.toLowerCase().trim();
    const statusFilter = document.getElementById('statusFilter').value;
    const typeFilter = document.getElementById('typeFilter').value;
    const employeeFilter = document.getElementById('employeeFilter').value;

    const filtered = allEquipment.filter(item => {
        // Поиск по тексту
        const matchSearch = !searchText ||
            item.name.toLowerCase().includes(searchText) ||
            (item.ipAddress && item.ipAddress.includes(searchText)) ||
            (item.macAddress && item.macAddress.toLowerCase().includes(searchText)) ||
            (item.serialNumber && item.serialNumber.toLowerCase().includes(searchText)) ||
            (item.address && item.address.toLowerCase().includes(searchText));

        // Фильтр по статусу
        const matchStatus = !statusFilter || item.status === statusFilter;

        // Фильтр по типу
        const matchType = !typeFilter || item.typeId == typeFilter;

        // Фильтр по ответственному
        const matchEmployee = !employeeFilter || item.employeeId == employeeFilter;

        return matchSearch && matchStatus && matchType && matchEmployee;
    });

    displayEquipment(filtered);
}

// Сброс фильтров
function resetFilters() {
    document.getElementById('searchInput').value = '';
    document.getElementById('statusFilter').value = '';
    document.getElementById('typeFilter').value = '';
    document.getElementById('employeeFilter').value = '';
    displayEquipment(allEquipment);
}

// Обновление статистики
function updateStats() {
    const activeCount = allEquipment.filter(e => e.status === 'Active').length;
    const maintenanceCount = allEquipment.filter(e => e.status === 'Maintenance').length;

    document.getElementById('totalCount').textContent = allEquipment.length;
    document.getElementById('activeCount').textContent = activeCount;
    document.getElementById('maintenanceCount').textContent = maintenanceCount;
    document.getElementById('displayedCount').textContent = allEquipment.length;
}

// Открытие модального окна для добавления
function openModal() {
    document.getElementById('modalTitle').textContent = 'Добавить оборудование';
    document.getElementById('equipmentForm').reset();
    document.getElementById('equipmentId').value = '';
    document.getElementById('status').value = 'Active';
    document.getElementById('modal').classList.add('active');
    document.getElementById('typeId').focus();
}

// Закрытие модального окна
function closeModal() {
    document.getElementById('modal').classList.remove('active');
    document.getElementById('equipmentForm').reset();
}

// Закрытие модального окна просмотра
function closeViewModal() {
    document.getElementById('viewModal').classList.remove('active');
}

// Редактирование оборудования
async function editEquipment(id) {
    try {
        const item = await api.get(`/equipment/${id}`);

        document.getElementById('modalTitle').textContent = 'Редактировать оборудование';
        document.getElementById('equipmentId').value = item.id;
        document.getElementById('typeId').value = item.typeId;
        document.getElementById('employeeId').value = item.employeeId || '';
        document.getElementById('name').value = item.name;
        document.getElementById('serialNumber').value = item.serialNumber;
        document.getElementById('ipAddress').value = item.ipAddress || '';
        document.getElementById('macAddress').value = item.macAddress || '';
        document.getElementById('address').value = item.address || '';
        document.getElementById('status').value = item.status;
        document.getElementById('technicalParams').value = item.technicalParams ?
            JSON.stringify(item.technicalParams, null, 2) : '';

        document.getElementById('modal').classList.add('active');
        document.getElementById('name').focus();
    } catch (error) {
        formUtils.showError('Ошибка загрузки оборудования: ' + error.message);
    }
}

// Сохранение оборудования (создание или обновление)
async function saveEquipment(event) {
    event.preventDefault();

    const id = document.getElementById('equipmentId').value;

    // Валидация и парсинг JSON
    let technicalParams = null;
    const paramsText = document.getElementById('technicalParams').value.trim();
    if (paramsText) {
        try {
            technicalParams = JSON.parse(paramsText);
        } catch (e) {
            formUtils.showError('❌ Ошибка в формате JSON технических параметров: ' + e.message);
            return;
        }
    }

    const data = {
        typeId: parseInt(document.getElementById('typeId').value),
        employeeId: document.getElementById('employeeId').value ?
            parseInt(document.getElementById('employeeId').value) : null,
        name: document.getElementById('name').value.trim(),
        serialNumber: document.getElementById('serialNumber').value.trim(),
        ipAddress: document.getElementById('ipAddress').value.trim() || null,
        macAddress: document.getElementById('macAddress').value.trim() || null,
        address: document.getElementById('address').value.trim() || null,
        status: document.getElementById('status').value,
        technicalParams: technicalParams
    };

    try {
        if (id) {
            // Обновление
            await api.put(`/equipment/${id}`, data);
            formUtils.showSuccess('✅ Оборудование успешно обновлено');
        } else {
            // Создание
            await api.post('/equipment', data);
            formUtils.showSuccess('✅ Оборудование успешно создано');
        }

        closeModal();
        await loadEquipment();
    } catch (error) {
        formUtils.showError('❌ Ошибка сохранения: ' + error.message);
    }
}

// Просмотр деталей оборудования
async function viewEquipment(id) {
    try {
        const item = await api.get(`/equipment/${id}`);

        document.getElementById('viewTitle').textContent = `🖥️ ${item.name}`;

        const content = `
            <div class="equipment-details">
                <div class="details-grid">
                    <div class="details-section">
                        <h3>📦 Основная информация</h3>
                        <div class="detail-row">
                            <span class="detail-label">ID:</span>
                            <span class="detail-value">${item.id}</span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">Название:</span>
                            <span class="detail-value"><strong>${escapeHtml(item.name)}</strong></span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">Тип:</span>
                            <span class="detail-value">${item.typeName ? escapeHtml(item.typeName) : 'N/A'}</span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">Производитель:</span>
                            <span class="detail-value">${item.manufacturer ? escapeHtml(item.manufacturer) : 'N/A'}</span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">Модель:</span>
                            <span class="detail-value">${item.model ? escapeHtml(item.model) : 'N/A'}</span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">Серийный номер:</span>
                            <span class="detail-value">${escapeHtml(item.serialNumber)}</span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">Статус:</span>
                            <span class="detail-value">${getStatusBadge(item.status)}</span>
                        </div>
                    </div>

                    <div class="details-section">
                        <h3>🌐 Сетевые параметры</h3>
                        <div class="detail-row">
                            <span class="detail-label">IP-адрес:</span>
                            <span class="detail-value">${item.ipAddress ? escapeHtml(item.ipAddress) : '<span class="text-muted">Не указан</span>'}</span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">MAC-адрес:</span>
                            <span class="detail-value">${item.macAddress ? escapeHtml(item.macAddress) : '<span class="text-muted">Не указан</span>'}</span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">Расположение:</span>
                            <span class="detail-value">${item.address ? escapeHtml(item.address) : '<span class="text-muted">Не указано</span>'}</span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">Ответственный:</span>
                            <span class="detail-value">${item.employeeFullName ? escapeHtml(item.employeeFullName) : '<span class="text-muted">Не назначен</span>'}</span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">Дата добавления:</span>
                            <span class="detail-value">${item.dateAdded ? formatDate(item.dateAdded) : 'N/A'}</span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">Дата обновления:</span>
                            <span class="detail-value">${item.dateUpdated ? formatDate(item.dateUpdated) : 'N/A'}</span>
                        </div>
                    </div>
                </div>

                <div class="stats-section">
                    <h3>📊 Статистика</h3>
                    <div class="stats-mini">
                        <div class="stat-mini-card">
                            <span class="stat-mini-label">Портов:</span>
                            <span class="stat-mini-value">${item.portsCount || 0}</span>
                        </div>
                        <div class="stat-mini-card">
                            <span class="stat-mini-label">IP-адресов:</span>
                            <span class="stat-mini-value">${item.ipAddressesCount || 0}</span>
                        </div>
                        <div class="stat-mini-card">
                            <span class="stat-mini-label">Обслуживаний:</span>
                            <span class="stat-mini-value">${item.maintenanceCount || 0}</span>
                        </div>
                    </div>
                </div>
                
                ${item.technicalParams ? `
                    <div class="tech-params-section">
                        <h3>⚙️ Технические параметры</h3>
                        <pre class="json-display">${JSON.stringify(item.technicalParams, null, 2)}</pre>
                    </div>
                ` : ''}
            </div>
            
            <div class="action-buttons">
                <button class="btn btn-primary" onclick="closeViewModal(); editEquipment(${item.id})">✏️ Редактировать</button>
                <button class="btn btn-secondary" onclick="closeViewModal()">Закрыть</button>
            </div>
        `;

        document.getElementById('viewContent').innerHTML = content;
        document.getElementById('viewModal').classList.add('active');
    } catch (error) {
        formUtils.showError('Ошибка загрузки данных: ' + error.message);
    }
}

// Удаление оборудования
async function deleteEquipment(id) {
    const item = allEquipment.find(e => e.id === id);
    if (!item) return;

    const confirmMessage = `Вы уверены, что хотите удалить оборудование "${item.name}"?\n\nЭто также удалит:\n- Все связанные порты\n- Все IP-адреса\n- Историю обслуживания`;

    if (!formUtils.confirm(confirmMessage)) {
        return;
    }

    try {
        await api.delete(`/equipment/${id}`);
        formUtils.showSuccess('✅ Оборудование успешно удалено');
        await loadEquipment();
    } catch (error) {
        formUtils.showError('❌ Ошибка удаления: ' + error.message);
    }
}

// Валидация JSON
function validateJSON() {
    const paramsText = document.getElementById('technicalParams').value.trim();

    if (!paramsText) {
        formUtils.showSuccess('✅ Поле пустое - это допустимо');
        return;
    }

    try {
        JSON.parse(paramsText);
        formUtils.showSuccess('✅ JSON корректен');
    } catch (e) {
        formUtils.showError('❌ Ошибка в JSON: ' + e.message);
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

// Обрезка текста
function truncateText(text, maxLength) {
    if (!text) return '';
    if (text.length <= maxLength) return text;
    return text.substring(0, maxLength) + '...';
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
    const viewModal = document.getElementById('viewModal');

    if (event.target === modal) {
        closeModal();
    }
    if (event.target === viewModal) {
        closeViewModal();
    }
});

// Закрытие по Escape
document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape') {
        closeModal();
        closeViewModal();
    }
});
