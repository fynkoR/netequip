let allMaintenance = [];
let allEquipment = [];
let allEmployees = [];
let currentMaintenanceId = null;

// Загрузка данных при старте
document.addEventListener('DOMContentLoaded', () => {
    loadEquipment();
    loadEmployees();
    loadMaintenance();
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

// Загрузка списка сотрудников
async function loadEmployees() {
    try {
        allEmployees = await api.get('/employees');
        populateEmployeeSelect();
        populateEmployeeFilter();
    } catch (error) {
        console.error('Ошибка загрузки сотрудников:', error);
        formUtils.showError('Ошибка загрузки списка сотрудников');
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

// Заполнение select сотрудников в форме
function populateEmployeeSelect() {
    const select = document.getElementById('performedById');
    select.innerHTML = '<option value="">Не указан</option>' +
        allEmployees.map(emp =>
            `<option value="${emp.id}">${escapeHtml(emp.fullName)}</option>`
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

// Заполнение фильтра сотрудников
function populateEmployeeFilter() {
    const select = document.getElementById('employeeFilter');
    const currentValue = select.value;

    select.innerHTML = '<option value="">Все сотрудники</option>' +
        allEmployees.map(emp =>
            `<option value="${emp.id}">${escapeHtml(emp.fullName)}</option>`
        ).join('');

    select.value = currentValue;
}

// Загрузка всех записей об обслуживании
async function loadMaintenance() {
    try {
        allMaintenance = await api.get('/maintenance-history');
        displayMaintenance(allMaintenance);
        updateStats();
    } catch (error) {
        formUtils.showError('Ошибка загрузки истории обслуживания: ' + error.message);
        document.getElementById('maintenanceBody').innerHTML =
            '<tr><td colspan="9" class="error">Ошибка загрузки данных</td></tr>';
    }
}

// Отображение записей в таблице
function displayMaintenance(maintenance) {
    const tbody = document.getElementById('maintenanceBody');

    if (maintenance.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" class="no-data">Нет данных</td></tr>';
        document.getElementById('displayedCount').textContent = '0';
        return;
    }

    tbody.innerHTML = maintenance.map(m => `
        <tr ${isOverdue(m.nextMaintenanceDate) ? 'class="overdue-row"' : ''}>
            <td>${m.id}</td>
            <td>${formatDateTime(m.date)}</td>
            <td>${m.equipmentName ? escapeHtml(m.equipmentName) : '<span class="text-muted">N/A</span>'}</td>
            <td>${getTypeBadge(m.type)}</td>
            <td class="description-cell">${m.description ? escapeHtml(truncateText(m.description, 50)) : '<span class="text-muted">-</span>'}</td>
            <td>${m.performedByName ? escapeHtml(m.performedByName) : '<span class="text-muted">Не указан</span>'}</td>
            <td>${m.cost ? formatCurrency(m.cost) : '<span class="text-muted">-</span>'}</td>
            <td>${formatNextMaintenanceDate(m.nextMaintenanceDate)}</td>
            <td>
                <button class="btn btn-info btn-small" onclick="viewDetails(${m.id})" title="Просмотр">👁️</button>
                <button class="btn btn-primary btn-small" onclick="editMaintenance(${m.id})" title="Редактировать">✏️</button>
                <button class="btn btn-danger btn-small" onclick="deleteMaintenance(${m.id})" title="Удалить">🗑️</button>
            </td>
        </tr>
    `).join('');

    document.getElementById('displayedCount').textContent = maintenance.length;
}

// Получение бейджа типа обслуживания
function getTypeBadge(type) {
    const badges = {
        'Routine': '<span class="badge badge-info">Routine</span>',
        'Repair': '<span class="badge badge-warning">Repair</span>',
        'Upgrade': '<span class="badge badge-success">Upgrade</span>',
        'Emergency': '<span class="badge badge-danger">Emergency</span>',
        'Preventive': '<span class="badge badge-secondary">Preventive</span>'
    };
    return badges[type] || `<span class="badge badge-secondary">${escapeHtml(type)}</span>`;
}

// Проверка просроченности
function isOverdue(nextDate) {
    if (!nextDate) return false;
    const today = new Date();
    const next = new Date(nextDate);
    return next < today;
}

// Форматирование даты следующего обслуживания
function formatNextMaintenanceDate(dateString) {
    if (!dateString) return '<span class="text-muted">Не запланировано</span>';

    const date = new Date(dateString);
    const formatted = formatDate(dateString);

    if (isOverdue(dateString)) {
        return `<span class="text-danger">⚠️ ${formatted}</span>`;
    }

    return formatted;
}

// Фильтрация таблицы
function filterTable() {
    const searchText = document.getElementById('searchInput').value.toLowerCase().trim();
    const equipmentFilter = document.getElementById('equipmentFilter').value;
    const typeFilter = document.getElementById('typeFilter').value;
    const employeeFilter = document.getElementById('employeeFilter').value;

    const filtered = allMaintenance.filter(m => {
        // Поиск по тексту
        const matchSearch = !searchText ||
            (m.equipmentName && m.equipmentName.toLowerCase().includes(searchText)) ||
            (m.description && m.description.toLowerCase().includes(searchText)) ||
            (m.performedByName && m.performedByName.toLowerCase().includes(searchText)) ||
            (m.type && m.type.toLowerCase().includes(searchText));

        // Фильтр по оборудованию
        const matchEquipment = !equipmentFilter || m.equipmentId == equipmentFilter;

        // Фильтр по типу
        const matchType = !typeFilter || m.type === typeFilter;

        // Фильтр по сотруднику
        const matchEmployee = !employeeFilter || m.performedById == employeeFilter;

        return matchSearch && matchEquipment && matchType && matchEmployee;
    });

    displayMaintenance(filtered);
}

// Фильтрация по диапазону дат
function filterByDateRange() {
    const days = parseInt(document.getElementById('dateRangeFilter').value);

    if (!days) {
        displayMaintenance(allMaintenance);
        return;
    }

    const now = new Date();
    const startDate = new Date();
    startDate.setDate(now.getDate() - days);

    const filtered = allMaintenance.filter(m => {
        if (!m.date) return false;
        const maintenanceDate = new Date(m.date);
        return maintenanceDate >= startDate && maintenanceDate <= now;
    });

    displayMaintenance(filtered);
}

// Сброс фильтров
function resetFilters() {
    document.getElementById('searchInput').value = '';
    document.getElementById('equipmentFilter').value = '';
    document.getElementById('typeFilter').value = '';
    document.getElementById('employeeFilter').value = '';
    document.getElementById('dateRangeFilter').value = '';
    displayMaintenance(allMaintenance);
}

// Обновление статистики
function updateStats() {
    const overdueCount = allMaintenance.filter(m => isOverdue(m.nextMaintenanceDate)).length;
    const totalCost = allMaintenance.reduce((sum, m) => sum + (parseFloat(m.cost) || 0), 0);

    document.getElementById('totalCount').textContent = allMaintenance.length;
    document.getElementById('overdueCount').textContent = overdueCount;
    document.getElementById('totalCost').textContent = formatCurrency(totalCost);
    document.getElementById('displayedCount').textContent = allMaintenance.length;
}

// Открытие модального окна для добавления
function openModal() {
    document.getElementById('modalTitle').textContent = 'Добавить запись об обслуживании';
    document.getElementById('maintenanceForm').reset();
    document.getElementById('maintenanceId').value = '';

    // Установка текущей даты и времени
    const now = new Date();
    const dateTimeString = now.toISOString().slice(0, 16);
    document.getElementById('date').value = dateTimeString;

    document.getElementById('modal').classList.add('active');
    document.getElementById('equipmentId').focus();
}

// Закрытие модального окна
function closeModal() {
    document.getElementById('modal').classList.remove('active');
    document.getElementById('maintenanceForm').reset();
}

// Просмотр деталей обслуживания
async function viewDetails(id) {
    try {
        const m = await api.get(`/maintenance-history/${id}`);
        currentMaintenanceId = id;

        document.getElementById('detailId').textContent = m.id;
        document.getElementById('detailEquipmentName').textContent = m.equipmentName || 'N/A';
        document.getElementById('detailDate').textContent = formatDateTime(m.date);
        document.getElementById('detailType').innerHTML = getTypeBadge(m.type);
        document.getElementById('detailPerformedBy').textContent = m.performedByName || 'Не указан';
        document.getElementById('detailCost').textContent = m.cost ? formatCurrency(m.cost) : 'Не указана';
        document.getElementById('detailNextDate').innerHTML = formatNextMaintenanceDate(m.nextMaintenanceDate);
        document.getElementById('detailDescription').textContent = m.description || 'Нет описания';

        document.getElementById('detailsModal').classList.add('active');
    } catch (error) {
        formUtils.showError('Ошибка загрузки данных обслуживания: ' + error.message);
    }
}

// Закрытие модального окна деталей
function closeDetailsModal() {
    document.getElementById('detailsModal').classList.remove('active');
    currentMaintenanceId = null;
}

// Редактирование из модального окна деталей
function editFromDetails() {
    closeDetailsModal();
    if (currentMaintenanceId) {
        editMaintenance(currentMaintenanceId);
    }
}

// Редактирование обслуживания
async function editMaintenance(id) {
    try {
        const m = await api.get(`/maintenance-history/${id}`);

        document.getElementById('modalTitle').textContent = 'Редактировать запись об обслуживании';
        document.getElementById('maintenanceId').value = m.id;
        document.getElementById('equipmentId').value = m.equipmentId;
        document.getElementById('date').value = m.date ? m.date.slice(0, 16) : '';
        document.getElementById('type').value = m.type;
        document.getElementById('description').value = m.description || '';
        document.getElementById('performedById').value = m.performedById || '';
        document.getElementById('cost').value = m.cost || '';
        document.getElementById('nextMaintenanceDate').value = m.nextMaintenanceDate || '';

        document.getElementById('modal').classList.add('active');
        document.getElementById('description').focus();
    } catch (error) {
        formUtils.showError('Ошибка загрузки данных обслуживания: ' + error.message);
    }
}

// Сохранение обслуживания (создание или обновление)
async function saveMaintenance(event) {
    event.preventDefault();

    const id = document.getElementById('maintenanceId').value;

    const data = {
        equipmentId: parseInt(document.getElementById('equipmentId').value),
        date: document.getElementById('date').value,
        type: document.getElementById('type').value,
        description: document.getElementById('description').value.trim(),
        performedById: document.getElementById('performedById').value ?
            parseInt(document.getElementById('performedById').value) : null,
        cost: document.getElementById('cost').value ?
            parseFloat(document.getElementById('cost').value) : null,
        nextMaintenanceDate: document.getElementById('nextMaintenanceDate').value || null
    };

    try {
        if (id) {
            // Обновление
            await api.put(`/maintenance-history/${id}`, data);
            formUtils.showSuccess('✅ Запись об обслуживании успешно обновлена');
        } else {
            // Создание
            await api.post('/maintenance-history', data);
            formUtils.showSuccess('✅ Запись об обслуживании успешно создана');
        }

        closeModal();
        await loadMaintenance();
    } catch (error) {
        formUtils.showError('❌ Ошибка сохранения: ' + error.message);
    }
}

// Удаление обслуживания
async function deleteMaintenance(id) {
    const m = allMaintenance.find(maintenance => maintenance.id === id);
    if (!m) return;

    const confirmMessage = `Вы уверены, что хотите удалить запись об обслуживании оборудования "${m.equipmentName}" от ${formatDateTime(m.date)}?`;

    if (!formUtils.confirm(confirmMessage)) {
        return;
    }

    try {
        await api.delete(`/maintenance-history/${id}`);
        formUtils.showSuccess('✅ Запись об обслуживании успешно удалена');
        await loadMaintenance();
    } catch (error) {
        formUtils.showError('❌ Ошибка удаления: ' + error.message);
    }
}

// Форматирование даты и времени
function formatDateTime(dateTimeString) {
    if (!dateTimeString) return '-';
    const date = new Date(dateTimeString);
    return date.toLocaleString('ru-RU', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
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

// Форматирование валюты
function formatCurrency(amount) {
    if (!amount) return '0 ₽';
    return new Intl.NumberFormat('ru-RU', {
        style: 'currency',
        currency: 'RUB',
        minimumFractionDigits: 2
    }).format(amount);
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
