// Базовый URL API
const API_BASE_URL = 'http://localhost:8080/api';

// Утилита для API запросов
const api = {
    // GET запрос
    async get(endpoint) {
        const response = await fetch(`${API_BASE_URL}${endpoint}`);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return await response.json();
    },

    // POST запрос
    async post(endpoint, data) {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data)
        });
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || `HTTP error! status: ${response.status}`);
        }
        return await response.json();
    },

    // PUT запрос
    async put(endpoint, data) {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data)
        });
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || `HTTP error! status: ${response.status}`);
        }
        return await response.json();
    },

    // PATCH запрос
    async patch(endpoint, data = null) {
        const options = {
            method: 'PATCH',
            headers: {}
        };

        if (data) {
            options.headers['Content-Type'] = 'application/json';
            options.body = JSON.stringify(data);
        }

        const response = await fetch(`${API_BASE_URL}${endpoint}`, options);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        // PATCH может возвращать пустое тело
        const text = await response.text();
        return text ? JSON.parse(text) : null;
    },

    // DELETE запрос
    async delete(endpoint) {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            method: 'DELETE'
        });
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return true;
    }
};

// Утилиты для форм
const formUtils = {
    // Показать ошибку
    showError(message) {
        const errorDiv = document.createElement('div');
        errorDiv.className = 'alert alert-error';
        errorDiv.textContent = message;
        document.querySelector('.container').insertBefore(errorDiv, document.querySelector('.container').firstChild);
        setTimeout(() => errorDiv.remove(), 5000);
    },

    // Показать успех
    showSuccess(message) {
        const successDiv = document.createElement('div');
        successDiv.className = 'alert alert-success';
        successDiv.textContent = message;
        document.querySelector('.container').insertBefore(successDiv, document.querySelector('.container').firstChild);
        setTimeout(() => successDiv.remove(), 3000);
    },

    // Очистить форму
    clearForm(formId) {
        document.getElementById(formId).reset();
    },

    // Подтверждение действия
    confirm(message) {
        return window.confirm(message);
    }
};
