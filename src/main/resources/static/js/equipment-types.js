let allTypes = [];

// Загрузка типов при старте
document.addEventListener('DOMContentLoaded', () => {
    loadEquipmentTypes();
});

// Загрузка всех типов оборудования
async function loadEquipmentTypes() {
    try {
        allTypes = await api.get('/equipment-types');
        displayTypes(allTypes);
        updateStats();
        populateManufacturerFilter();
    } catch (error) {
        formUtils.showError('Ошибка загрузки типов оборудования: ' + error.message);
        document.getElementById('typesBody').innerHTML =
            '<tr><td colspan="8" class="error">Ошибка загрузки данных</td></tr>';
    }
}

// Отображение типов в таблице
function displayTypes(types) {
    const tbody = document.getElementById('typesBody');

    if (types.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="no-data">Нет данных</td></tr>';
        updateDisplayedCount(0);
        return;
    }

    tbody.innerHTML = types.map(type => `
        <tr>
            <td>${type.id}</td>
            <td><strong>${escapeHtml(type.typeName)}</strong></td>
            <td>${escapeHtml(type.manufacturer)}</td>
            <td>${escapeHtml(type.model)}</td>
            <td>${type.defaultPortCount || '<span class="text-muted">-</span>'}</td>
            <td>${type.connectionType ? escapeHtml(type.connectionType) : '<span class="text-muted">-</span>'}</td>
            <td>${type.osiLevel ? `<span class="badge badge-info">${escapeHtml(type.osiLevel)}</span>` : '<span class="text-muted">-</span>'}</td>
            <td>
                <button class="btn btn-info btn-small" onclick="viewDetails(${type.id})" title="Просмотр">👁️</button>
                <button class="btn btn-primary btn-small" onclick="editType(${type.id})" title="Редактировать">✏️</button>
                <button class="btn btn-danger btn-small" onclick="deleteType(${type.id})" title="Удалить">🗑️</button>
            </td>
        </tr>
    `).join('');

    updateDisplayedCount(types.length);
}

// Фильтрация таблицы
function filterTable() {
    const searchText = document.getElementById('searchInput').value.toLowerCase().trim();
    const osiFilter = document.getElementById('osiFilter').value;
    const manufacturerFilter = document.getElementById('manufacturerFilter')?.value;

    const filtered = allTypes.filter(type => {
        // Поиск по тексту
        const matchSearch = !searchText ||
            type.typeName.toLowerCase().includes(searchText) ||
            type.manufacturer.toLowerCase().includes(searchText) ||
            type.model.toLowerCase().includes(searchText) ||
            (type.connectionType && type.connectionType.toLowerCase().includes(searchText)) ||
            (type.description && type.description.toLowerCase().includes(searchText));

        // Фильтр по уровню OSI
        const matchOsi = !osiFilter || type.osiLevel === osiFilter;

        // Фильтр по производителю
        const matchManufacturer = !manufacturerFilter || type.manufacturer === manufacturerFilter;

        return matchSearch && matchOsi && matchManufacturer;
    });

    displayTypes(filtered);
}

// Сброс фильтров
function resetFilters() {
    document.getElementById('searchInput').value = '';
    document.getElementById('osiFilter').value = '';
    if (document.getElementById('manufacturerFilter')) {
        document.getElementById('manufacturerFilter').value = '';
    }
    displayTypes(allTypes);
}

// Обновление статистики
function updateStats() {
    const totalCount = allTypes.length;
    const manufacturersCount = new Set(allTypes.map(t => t.manufacturer)).size;

    if (document.getElementById('totalCount')) {
        document.getElementById('totalCount').textContent = totalCount;
    }
    if (document.getElementById('manufacturersCount')) {
        document.getElementById('manufacturersCount').textContent = manufacturersCount;
    }
}

// Обновление счетчика отображаемых записей
function updateDisplayedCount(count) {
    if (document.getElementById('displayedCount')) {
        document.getElementById('displayedCount').textContent = count;
    }
}

// Заполнение фильтра производителей
function populateManufacturerFilter() {
    const select = document.getElementById('manufacturerFilter');
    if (!select) return;

    const manufacturers = [...new Set(allTypes.map(t => t.manufacturer))].sort();
    const currentValue = select.value;

    select.innerHTML = '<option value="">Все производители</option>' +
        manufacturers.map(m => `<option value="${escapeHtml(m)}">${escapeHtml(m)}</option>`).join('');

    select.value = currentValue;
}

// Открытие модального окна для добавления
function openModal() {
    document.getElementById('modalTitle').textContent = 'Добавить тип оборудования';
    document.getElementById('equipmentTypeForm').reset();
    document.getElementById('typeId').value = '';
    document.getElementById('modal').classList.add('active');
    document.getElementById('typeName').focus();
}

// Закрытие модального окна
function closeModal() {
    document.getElementById('modal').classList.remove('active');
    document.getElementById('equipmentTypeForm').reset();
}

// Просмотр деталей типа
async function viewDetails(id) {
    try {
        const type = await api.get(`/equipment-types/${id}`);

        const content = `
            <div class="details-content">
                <div class="detail-row">
                    <span class="detail-label">ID:</span>
                    <span class="detail-value">${type.id}</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Название типа:</span>
                    <span class="detail-value"><strong>${escapeHtml(type.typeName)}</strong></span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Производитель:</span>
                    <span class="detail-value">${escapeHtml(type.manufacturer)}</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Модель:</span>
                    <span class="detail-value">${escapeHtml(type.model)}</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Количество портов:</span>
                    <span class="detail-value">${type.defaultPortCount || 'Не указано'}</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Тип подключения:</span>
                    <span class="detail-value">${type.connectionType ? escapeHtml(type.connectionType) : 'Не указан'}</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Уровень OSI:</span>
                    <span class="detail-value">${type.osiLevel ? `<span class="badge badge-info">${escapeHtml(type.osiLevel)}</span>` : 'Не указан'}</span>
                </div>
                ${type.description ? `
                    <div class="detail-row detail-full">
                        <span class="detail-label">Описание:</span>
                    </div>
                    <div class="detail-description">${escapeHtml(type.description)}</div>
                ` : ''}
            </div>
            <div class="action-buttons">
                <button class="btn btn-primary" onclick="closeDetailsModal(); editType(${type.id})">✏️ Редактировать</button>
                <button class="btn btn-secondary" onclick="closeDetailsModal()">Закрыть</button>
            </div>
        `;

        document.getElementById('detailsModalContent').innerHTML = content;
        document.getElementById('detailsModal').classList.add('active');
    } catch (error) {
        formUtils.showError('Ошибка загрузки данных типа: ' + error.message);
    }
}

// Закрытие модального окна деталей
function closeDetailsModal() {
    document.getElementById('detailsModal').classList.remove('active');
}

// Редактирование типа
async function editType(id) {
    try {
        const type = await api.get(`/equipment-types/${id}`);

        document.getElementById('modalTitle').textContent = 'Редактировать тип оборудования';
        document.getElementById('typeId').value = type.id;
        document.getElementById('typeName').value = type.typeName;
        document.getElementById('manufacturer').value = type.manufacturer;
        document.getElementById('model').value = type.model;
        document.getElementById('defaultPortCount').value = type.defaultPortCount || '';
        document.getElementById('connectionType').value = type.connectionType || '';
        document.getElementById('osiLevel').value = type.osiLevel || '';
        document.getElementById('description').value = type.description || '';

        document.getElementById('modal').classList.add('active');
        document.getElementById('typeName').focus();
    } catch (error) {
        formUtils.showError('Ошибка загрузки типа: ' + error.message);
    }
}

// Сохранение типа оборудования
async function saveEquipmentType(event) {
    event.preventDefault();

    const id = document.getElementById('typeId').value;
    const typeName = document.getElementById('typeName').value.trim();
    const manufacturer = document.getElementById('manufacturer').value.trim();
    const model = document.getElementById('model').value.trim();

    // Проверка уникальности при создании
    if (!id) {
        try {
            const exists = await api.get(`/equipment-types/exists?typeName=${encodeURIComponent(typeName)}`);
            if (exists) {
                formUtils.showError('Тип оборудования с таким названием уже существует');
                return;
            }
        } catch (error) {
            console.error('Ошибка проверки существования:', error);
        }
    }

    const data = {
        typeName: typeName,
        manufacturer: manufacturer,
        model: model,
        defaultPortCount: document.getElementById('defaultPortCount').value ?
            parseInt(document.getElementById('defaultPortCount').value) : null,
        connectionType: document.getElementById('connectionType').value.trim() || null,
        osiLevel: document.getElementById('osiLevel').value || null,
        description: document.getElementById('description').value.trim() || null
    };

    try {
        if (id) {
            // Обновление
            await api.put(`/equipment-types/${id}`, data);
            formUtils.showSuccess('✅ Тип оборудования успешно обновлён');
        } else {
            // Создание
            await api.post('/equipment-types', data);
            formUtils.showSuccess('✅ Тип оборудования успешно создан');
        }

        closeModal();
        await loadEquipmentTypes();
        populateManufacturerFilter();
    } catch (error) {
        formUtils.showError('❌ Ошибка сохранения: ' + error.message);
    }
}

// Удаление типа
async function deleteType(id) {
    const type = allTypes.find(t => t.id === id);
    if (!type) return;

    const confirmMessage = `Вы уверены, что хотите удалить тип оборудования "${type.typeName}"?\n\nВНИМАНИЕ: Это также удалит всё оборудование этого типа!`;

    if (!formUtils.confirm(confirmMessage)) {
        return;
    }

    try {
        await api.delete(`/equipment-types/${id}`);
        formUtils.showSuccess('✅ Тип оборудования успешно удалён');
        await loadEquipmentTypes();
        populateManufacturerFilter();
    } catch (error) {
        formUtils.showError('❌ Ошибка удаления: ' + error.message);
    }
}

// Поиск по производителю и модели
async function searchByManufacturerAndModel() {
    const manufacturer = prompt('Введите название производителя:');
    if (!manufacturer) return;

    const model = prompt('Введите модель:');
    if (!model) return;

    try {
        const type = await api.get(`/equipment-types/search?manufacturer=${encodeURIComponent(manufacturer)}&model=${encodeURIComponent(model)}`);
        displayTypes([type]);
        formUtils.showSuccess(`Найден тип: ${type.typeName}`);
    } catch (error) {
        formUtils.showError('Тип не найден: ' + error.message);
        displayTypes(allTypes);
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
