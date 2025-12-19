let allTypes = [];

// Загрузка типов при старте
document.addEventListener('DOMContentLoaded', loadEquipmentTypes);

async function loadEquipmentTypes() {
    try {
        allTypes = await api.get('/equipment-types');
        displayTypes(allTypes);
    } catch (error) {
        formUtils.showError('Ошибка загрузки типов оборудования: ' + error.message);
    }
}

function displayTypes(types) {
    const tbody = document.getElementById('typesBody');

    if (types.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8">Нет данных</td></tr>';
        return;
    }

    tbody.innerHTML = types.map(type => `
        <tr>
            <td>${type.id}</td>
            <td><strong>${type.typeName}</strong></td>
            <td>${type.manufacturer}</td>
            <td>${type.model}</td>
            <td>${type.defaultPortCount || '-'}</td>
            <td>${type.connectionType || '-'}</td>
            <td><span class="badge badge-info">${type.osiLevel || '-'}</span></td>
            <td>
                <button class="btn btn-primary btn-small" onclick="editType(${type.id})">✏️ Изменить</button>
                <button class="btn btn-danger btn-small" onclick="deleteType(${type.id})">🗑️ Удалить</button>
            </td>
        </tr>
    `).join('');
}

function filterTable() {
    const searchText = document.getElementById('searchInput').value.toLowerCase();
    const osiFilter = document.getElementById('osiFilter').value;

    const filtered = allTypes.filter(type => {
        const matchSearch = !searchText ||
            type.typeName.toLowerCase().includes(searchText) ||
            type.manufacturer.toLowerCase().includes(searchText) ||
            type.model.toLowerCase().includes(searchText);

        const matchOsi = !osiFilter || type.osiLevel === osiFilter;

        return matchSearch && matchOsi;
    });

    displayTypes(filtered);
}

function openModal() {
    document.getElementById('modalTitle').textContent = 'Добавить тип оборудования';
    document.getElementById('equipmentTypeForm').reset();
    document.getElementById('typeId').value = '';
    document.getElementById('modal').classList.add('active');
}

function closeModal() {
    document.getElementById('modal').classList.remove('active');
}

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
    } catch (error) {
        formUtils.showError('Ошибка загрузки типа: ' + error.message);
    }
}

async function saveEquipmentType(event) {
    event.preventDefault();

    const id = document.getElementById('typeId').value;
    const data = {
        typeName: document.getElementById('typeName').value,
        manufacturer: document.getElementById('manufacturer').value,
        model: document.getElementById('model').value,
        defaultPortCount: parseInt(document.getElementById('defaultPortCount').value) || null,
        connectionType: document.getElementById('connectionType').value || null,
        osiLevel: document.getElementById('osiLevel').value || null,
        description: document.getElementById('description').value || null
    };

    try {
        if (id) {
            await api.put(`/equipment-types/${id}`, data);
            formUtils.showSuccess('Тип оборудования успешно обновлён');
        } else {
            await api.post('/equipment-types', data);
            formUtils.showSuccess('Тип оборудования успешно создан');
        }

        closeModal();
        loadEquipmentTypes();
    } catch (error) {
        formUtils.showError('Ошибка сохранения: ' + error.message);
    }
}

async function deleteType(id) {
    if (!formUtils.confirm('Вы уверены, что хотите удалить этот тип оборудования?')) {
        return;
    }

    try {
        await api.delete(`/equipment-types/${id}`);
        formUtils.showSuccess('Тип оборудования успешно удалён');
        loadEquipmentTypes();
    } catch (error) {
        formUtils.showError('Ошибка удаления: ' + error.message);
    }
}
