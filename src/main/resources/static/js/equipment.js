let allEquipment = [];
let equipmentTypes = [];
let employees = [];

document.addEventListener('DOMContentLoaded', async () => {
    await Promise.all([
        loadEquipment(),
        loadEquipmentTypes(),
        loadEmployees()
    ]);
});

async function loadEquipment() {
    try {
        allEquipment = await api.get('/equipment');
        displayEquipment(allEquipment);
    } catch (error) {
        formUtils.showError('Ошибка загрузки оборудования: ' + error.message);
    }
}

async function loadEquipmentTypes() {
    try {
        equipmentTypes = await api.get('/equipment-types');
        populateTypeSelects();
    } catch (error) {
        console.error('Ошибка загрузки типов:', error);
    }
}

async function loadEmployees() {
    try {
        employees = await api.get('/employees');
        populateEmployeeSelect();
    } catch (error) {
        console.error('Ошибка загрузки сотрудников:', error);
    }
}

function populateTypeSelects() {
    const formSelect = document.getElementById('typeId');
    const filterSelect = document.getElementById('typeFilter');

    equipmentTypes.forEach(type => {
        formSelect.innerHTML += `<option value="${type.id}">${type.typeName} - ${type.manufacturer} ${type.model}</option>`;
        filterSelect.innerHTML += `<option value="${type.id}">${type.typeName}</option>`;
    });
}

function populateEmployeeSelect() {
    const select = document.getElementById('employeeId');
    employees.forEach(emp => {
        select.innerHTML += `<option value="${emp.id}">${emp.lastName} ${emp.firstName} - ${emp.position}</option>`;
    });
}

function displayEquipment(equipment) {
    const tbody = document.getElementById('equipmentBody');

    if (equipment.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8">Нет данных</td></tr>';
        return;
    }

    tbody.innerHTML = equipment.map(item => `
        <tr>
            <td>${item.id}</td>
            <td><strong>${item.name}</strong><br><small>${item.serialNumber}</small></td>
            <td>${item.typeName || 'N/A'}</td>
            <td>${item.ipAddress || '-'}</td>
            <td><small>${item.macAddress || '-'}</small></td>
            <td>${item.employeeName || '-'}</td>
            <td><span class="badge badge-${getStatusClass(item.status)}">${item.status}</span></td>
            <td>
                <button class="btn btn-info btn-small" onclick="viewEquipment(${item.id})">👁️ Просмотр</button>
                <button class="btn btn-primary btn-small" onclick="editEquipment(${item.id})">✏️</button>
                <button class="btn btn-danger btn-small" onclick="deleteEquipment(${item.id})">🗑️</button>
            </td>
        </tr>
    `).join('');
}

function getStatusClass(status) {
    const map = {
        'Active': 'success',
        'Inactive': 'secondary',
        'Maintenance': 'warning',
        'Retired': 'danger'
    };
    return map[status] || 'secondary';
}

function filterTable() {
    const searchText = document.getElementById('searchInput').value.toLowerCase();
    const statusFilter = document.getElementById('statusFilter').value;
    const typeFilter = document.getElementById('typeFilter').value;

    const filtered = allEquipment.filter(item => {
        const matchSearch = !searchText ||
            item.name.toLowerCase().includes(searchText) ||
            (item.ipAddress && item.ipAddress.includes(searchText)) ||
            (item.macAddress && item.macAddress.toLowerCase().includes(searchText)) ||
            (item.serialNumber && item.serialNumber.toLowerCase().includes(searchText));

        const matchStatus = !statusFilter || item.status === statusFilter;
        const matchType = !typeFilter || item.typeId == typeFilter;

        return matchSearch && matchStatus && matchType;
    });

    displayEquipment(filtered);
}

function openModal() {
    document.getElementById('modalTitle').textContent = 'Добавить оборудование';
    document.getElementById('equipmentForm').reset();
    document.getElementById('equipmentId').value = '';
    document.getElementById('status').value = 'Active';
    document.getElementById('modal').classList.add('active');
}

function closeModal() {
    document.getElementById('modal').classList.remove('active');
}

function closeViewModal() {
    document.getElementById('viewModal').classList.remove('active');
}

async function editEquipment(id) {
    try {
        const item = await api.get(`/equipment/${id}`);

        document.getElementById('modalTitle').textContent = 'Редактировать оборудование';
        document.getElementById('equipmentId').value = item.id;
        document.getElementById('typeId').value = item.typeId;
        document.getElementById('employeeId').value = item.employeeId;
        document.getElementById('name').value = item.name;
        document.getElementById('serialNumber').value = item.serialNumber;
        document.getElementById('ipAddress').value = item.ipAddress || '';
        document.getElementById('macAddress').value = item.macAddress || '';
        document.getElementById('address').value = item.address || '';
        document.getElementById('status').value = item.status;
        document.getElementById('technicalParams').value = item.technicalParams ? JSON.stringify(item.technicalParams, null, 2) : '';

        document.getElementById('modal').classList.add('active');
    } catch (error) {
        formUtils.showError('Ошибка загрузки оборудования: ' + error.message);
    }
}

async function saveEquipment(event) {
    event.preventDefault();

    const id = document.getElementById('equipmentId').value;

    let technicalParams = null;
    const paramsText = document.getElementById('technicalParams').value.trim();
    if (paramsText) {
        try {
            technicalParams = JSON.parse(paramsText);
        } catch (e) {
            formUtils.showError('Ошибка в формате JSON технических параметров');
            return;
        }
    }

    const data = {
        typeId: parseInt(document.getElementById('typeId').value),
        employeeId: parseInt(document.getElementById('employeeId').value),
        name: document.getElementById('name').value,
        serialNumber: document.getElementById('serialNumber').value,
        ipAddress: document.getElementById('ipAddress').value || null,
        macAddress: document.getElementById('macAddress').value || null,
        address: document.getElementById('address').value || null,
        status: document.getElementById('status').value,
        technicalParams: technicalParams
    };

    try {
        if (id) {
            await api.put(`/equipment/${id}`, data);
            formUtils.showSuccess('Оборудование успешно обновлено');
        } else {
            await api.post('/equipment', data);
            formUtils.showSuccess('Оборудование успешно создано');
        }

        closeModal();
        loadEquipment();
    } catch (error) {
        formUtils.showError('Ошибка сохранения: ' + error.message);
    }
}

async function viewEquipment(id) {
    try {
        const item = await api.get(`/equipment/${id}`);

        document.getElementById('viewTitle').textContent = `Оборудование: ${item.name}`;

        const content = `
            <div style="display: grid; grid-template-columns: repeat(2, 1fr); gap: 2rem;">
                <div>
                    <h3>Основная информация</h3>
                    <p><strong>ID:</strong> ${item.id}</p>
                    <p><strong>Название:</strong> ${item.name}</p>
                    <p><strong>Тип:</strong> ${item.typeName}</p>
                    <p><strong>Производитель:</strong> ${item.manufacturer}</p>
                    <p><strong>Модель:</strong> ${item.model}</p>
                    <p><strong>Серийный номер:</strong> ${item.serialNumber}</p>
                    <p><strong>Статус:</strong> <span class="badge badge-${getStatusClass(item.status)}">${item.status}</span></p>
                </div>
                <div>
                    <h3>Сетевые параметры</h3>
                    <p><strong>IP-адрес:</strong> ${item.ipAddress || '-'}</p>
                    <p><strong>MAC-адрес:</strong> ${item.macAddress || '-'}</p>
                    <p><strong>Расположение:</strong> ${item.address || '-'}</p>
                    <p><strong>Ответственный:</strong> ${item.employeeName || '-'}</p>
                </div>
            </div>
            
            <div style="margin-top: 2rem;">
                <h3>Статистика</h3>
                <div class="stats-grid">
                    <div class="stat-card">
                        <div class="stat-value">${item.portsCount || 0}</div>
                        <div class="stat-label">Портов</div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-value">${item.ipAddressesCount || 0}</div>
                        <div class="stat-label">IP-адресов</div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-value">${item.maintenanceCount || 0}</div>
                        <div class="stat-label">Обслуживаний</div>
                    </div>
                </div>
            </div>
            
            ${item.technicalParams ? `
                <div style="margin-top: 2rem;">
                    <h3>Технические параметры</h3>
                    <pre style="background: var(--bg-color); padding: 1rem; border-radius: 0.5rem; overflow-x: auto;">${JSON.stringify(item.technicalParams, null, 2)}</pre>
                </div>
            ` : ''}
            
            <div style="margin-top: 2rem;">
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

async function deleteEquipment(id) {
    if (!formUtils.confirm('Вы уверены, что хотите удалить это оборудование?')) {
        return;
    }

    try {
        await api.delete(`/equipment/${id}`);
        formUtils.showSuccess('Оборудование успешно удалено');
        loadEquipment();
    } catch (error) {
        formUtils.showError('Ошибка удаления: ' + error.message);
    }
}
