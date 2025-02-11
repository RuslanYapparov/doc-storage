function includeHTML() {
    return new Promise((resolve, reject) => {
        const elements = document.querySelectorAll("[data-include-html]");
        const promises = Array.from(elements).map(async (el) => {
            const file = el.getAttribute("data-include-html");
            if (file) {
                try {
                    const response = await fetch(file);
                    if (response.ok) {
                        el.innerHTML = await response.text();
                    } else {
                        el.innerHTML = "Ошибка загрузки файла.";
                    }
                } catch {
                    el.innerHTML = "Ошибка подключения файла.";
                }
            }
        });

        Promise.all(promises)
            .then(() => resolve())
            .catch(reject);
    });
}

window.addEventListener("DOMContentLoaded", async () => {
    await includeHTML();
    initializePage();
    initializeNavigation();
});

function initializePage() {
    const requiredElements = {
        documentGrid: document.getElementById("documentGrid"),
        searchPattern: document.getElementById("searchPattern"),
        dateFrom: document.getElementById("dateFrom"),
        dateTo: document.getElementById("dateTo"),
        sortBy: document.getElementById("sortBy"),
        order: document.getElementById("order"),
        size: document.getElementById("size")
    };

    const missingElements = Object.entries(requiredElements)
        .filter(([_, element]) => !element)
        .map(([id]) => id);

    if (missingElements.length > 0) {
        console.warn('Отсутствуют необходимые элементы:', missingElements.join(', '));
        return; // Прерываем инициализацию если что-то отсутствует
    }

    loadMore(true);
    requiredElements.searchPattern.addEventListener("input", resetSearchFlag);
    requiredElements.dateFrom.addEventListener("change", resetSearchFlag);
    requiredElements.dateTo.addEventListener("change", resetSearchFlag);
    requiredElements.sortBy.addEventListener("change", resetSearchFlag);
    requiredElements.order.addEventListener("change", resetSearchFlag);
    requiredElements.size.addEventListener("input", resetSearchFlag);
}

function initializeNavigation() {
    const currentPath = window.location.pathname;

    const menuItems = document.querySelectorAll('.dropdown-menu .dropdown-item');

    menuItems.forEach(item => {
        item.classList.remove('active');
        if (item.getAttribute('href') === currentPath) {
            item.classList.add('active');
        }
    });
}