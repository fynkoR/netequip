let allEmployees = [];
let currentEmployeeId = null;

// Загрузка сотрудников при старте
document.addEventListener('DOMContentLoaded', () => {
    loadEmployees();
    loadPositionsList();
});

// Загрузка всех сотрудников
async function loadEmployees() {
    try {
        allEmployees = await api.get('/employees');
        displayEmployees(allEmployees);
        updateStats();
        populatePositionFilter();
    } catch (error) {
        formUtils.showError('Ошибка загрузки сотрудников: ' + error.message);
        document.getElementById('employeesBody').innerHTML =
            '<tr><td colspan="5" class="error">Ошибка загрузки данных</td></tr>';
    }
}

// Отображение сотрудников в таблице
function displayEmployees(employees) {
    const tbody = document.getElementById('employeesBody');

    if (employees.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="no-data">Нет данных</td></tr>';
        document.getElementById('displayedCount').textContent = '0';
        return;
    }

    tbody.innerHTML = employees.map(employee => `
        <tr>
            <td>${employee.id}</td>
            <td><strong>${escapeHtml(employee.fullName)}</strong></td>
            <td>${employee.position ? escapeHtml(employee.position) : '<span class="text-muted">Не указана</span>'}</td>
            <td>${employee.email ? `<a href="mailto:${employee.email}">${escapeHtml(employee.email)}</a>` : '<span class="text-muted">Не указан</span>'}</td>
            <td>
                <button class="btn btn-info btn-small" onclick="viewDetails(${employee.id})" title="Просмотр">👁️</button>
                <button class="btn btn-primary btn-small" onclick="editEmployee(${employee.id})" title="Редактировать">✏️</button>
                <button class="btn btn-danger btn-small" onclick="deleteEmployee(${employee.id})" title="Удалить">🗑️</button>
            </td>
        </tr>
    `).join('');

    document.getElementById('displayedCount').textContent = employees.length;
}

// Фильтрация таблицы
function filterTable() {
    const searchText = document.getElementById('searchInput').value.toLowerCase().trim();
    const positionFilter = document.getElementById('positionFilter').value;

    const filtered = allEmployees.filter(employee => {
        // Поиск по тексту
        const matchSearch = !searchText ||
            employee.fullName.toLowerCase().includes(searchText) ||
            (employee.position && employee.position.toLowerCase().includes(searchText)) ||
            (employee.email && employee.email.toLowerCase().includes(searchText));

        // Фильтр по должности
        const matchPosition = !positionFilter || employee.position === positionFilter;

        return matchSearch && matchPosition;
    });

    displayEmployees(filtered);
}

// Сброс фильтров
function resetFilters() {
    document.getElementById('searchInput').value = '';
    document.getElementById('positionFilter').value = '';
    displayEmployees(allEmployees);
}

// Обновление статистики
function updateStats() {
    document.getElementById('totalCount').textContent = allEmployees.length;
    document.getElementById('displayedCount').textContent = allEmployees.length;
}

// Заполнение фильтра должностей
function populatePositionFilter() {
    const positions = [...new Set(allEmployees
        .map(e => e.position)
        .filter(p => p)
    )].sort();

    const select = document.getElementById('positionFilter');
    const currentValue = select.value;

    select.innerHTML = '<option value="">Все должности</option>' +
        positions.map(pos => `<option value="${escapeHtml(pos)}">${escapeHtml(pos)}</option>`).join('');

    select.value = currentValue;
}

// Загрузка списка должностей для datalist
async function loadPositionsList() {
    try {
        const positions = [...new Set(allEmployees
            .map(e => e.position)
            .filter(p => p)
        )].sort();

        const datalist = document.getElementById('positionsList');
        datalist.innerHTML = positions.map(pos =>
            `<option value="${escapeHtml(pos)}">`
        ).join('');
    } catch (error) {
        console.error('Ошибка загрузки списка должностей:', error);
    }
}

// Открытие модального окна для добавления
function openModal() {
    document.getElementById('modalTitle').textContent = 'Добавить сотрудника';
    document.getElementById('employeeForm').reset();
    document.getElementById('employeeId').value = '';
    document.getElementById('modal').classList.add('active');
    document.getElementById('fullName').focus();
}

// Закрытие модального окна
function closeModal() {
    document.getElementById('modal').classList.remove('active');
    document.getElementById('employeeForm').reset();
}

// Просмотр деталей сотрудника
async function viewDetails(id) {
    try {
        const employee = await api.get(`/employees/${id}`);
        currentEmployeeId = id;

        document.getElementById('detailId').textContent = employee.id;
        document.getElementById('detailFullName').textContent = employee.fullName;
        document.getElementById('detailPosition').textContent = employee.position || 'Не указана';
        document.getElementById('detailEmail').textContent = employee.email || 'Не указан';

        document.getElementById('detailsModal').classList.add('active');
    } catch (error) {
        formUtils.showError('Ошибка загрузки данных сотрудника: ' + error.message);
    }
}

// Закрытие модального окна деталей
function closeDetailsModal() {
    document.getElementById('detailsModal').classList.remove('active');
    currentEmployeeId = null;
}

// Редактирование из модального окна деталей
function editFromDetails() {
    closeDetailsModal();
    if (currentEmployeeId) {
        editEmployee(currentEmployeeId);
    }
}

// Редактирование сотрудника
async function editEmployee(id) {
    try {
        const employee = await api.get(`/employees/${id}`);

        document.getElementById('modalTitle').textContent = 'Редактировать сотрудника';
        document.getElementById('employeeId').value = employee.id;
        document.getElementById('fullName').value = employee.fullName;
        document.getElementById('position').value = employee.position || '';
        document.getElementById('email').value = employee.email || '';

        document.getElementById('modal').classList.add('active');
        document.getElementById('fullName').focus();
    } catch (error) {
        formUtils.showError('Ошибка загрузки данных сотрудника: ' + error.message);
    }
}

// Сохранение сотрудника (создание или обновление)
async function saveEmployee(event) {
    event.preventDefault();

    const id = document.getElementById('employeeId').value;
    const fullName = document.getElementById('fullName').value.trim();
    const position = document.getElementById('position').value.trim();
    const email = document.getElementById('email').value.trim();

    // Валидация email на уникальность
    if (email && !id) {
        try {
            const exists = await api.get(`/employees/exists/email?email=${encodeURIComponent(email)}`);
            if (exists) {
                formUtils.showError('Сотрудник с таким email уже существует');
                return;
            }
        } catch (error) {
            console.error('Ошибка проверки email:', error);
        }
    }

    const data = {
        fullName: fullName,
        position: position || null,
        email: email || null
    };

    try {
        if (id) {
            // Обновление
            await api.put(`/employees/${id}`, data);
            formUtils.showSuccess('✅ Сотрудник успешно обновлён');
        } else {
            // Создание
            await api.post('/employees', data);
            formUtils.showSuccess('✅ Сотрудник успешно создан');
        }

        closeModal();
        await loadEmployees();
        loadPositionsList();
    } catch (error) {
        formUtils.showError('❌ Ошибка сохранения: ' + error.message);
    }
}

// Удаление сотрудника
async function deleteEmployee(id) {
    const employee = allEmployees.find(e => e.id === id);
    if (!employee) return;

    const confirmMessage = `Вы уверены, что хотите удалить сотрудника "${employee.fullName}"?`;

    if (!formUtils.confirm(confirmMessage)) {
        return;
    }

    try {
        await api.delete(`/employees/${id}`);
        formUtils.showSuccess('✅ Сотрудник успешно удалён');
        await loadEmployees();
        populatePositionFilter();
        loadPositionsList();
    } catch (error) {
        formUtils.showError('❌ Ошибка удаления: ' + error.message);
    }
}

// Поиск по имени (дополнительная функция)
async function searchByName(name) {
    try {
        const results = await api.get(`/employees/search?name=${encodeURIComponent(name)}`);
        displayEmployees(results);
    } catch (error) {
        formUtils.showError('Ошибка поиска: ' + error.message);
    }
}

// Экранирование HTML для предотвращения XSS
function escapeHtml(text) {
    const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    };
    return text.replace(/[&<>"']/g, m => map[m]);
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
