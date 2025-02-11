let isFirstSearch = true;

function resetSearchFlag() {
    isFirstSearch = true;
}

function toggleSortMenu() {
    const menu = document.getElementById('sortMenu');
    menu.style.display = menu.style.display === 'none' ? 'block' : 'none';
}

function toggleSearchMenu() {
    const searchMenu = document.getElementById("searchMenu");
    const loadMoreButton = document.getElementById("loadMoreButton");

    if (searchMenu.style.display === 'none') {
        searchMenu.style.display = 'block';
        loadMoreButton.disabled = true;
    } else {
        searchMenu.style.display = 'none';
        loadMoreButton.disabled = false;
    }
}

function addDocumentsToGrid(docs) {
    const grid = document.getElementById("documentGrid");

    docs.forEach((doc) => {
        // Проверяем, существует ли строка с таким документом
        const existingRow = Array.from(grid.rows).find(
            (row) => row.dataset.id === String(doc.id)
        );

        if (!existingRow) {
            const row = grid.insertRow();
            row.dataset.id = doc.id;

            row.innerHTML = `
            <td>${doc.title || ""}</td>
            <td>${doc.description || ""}</td>
            <td>${doc.ownerName || ""}</td>
            <td>${doc.fileName || ""}</td>
            <td>${doc.createdAt || ""}</td>
            <td>${doc.commonAccessType || ""}</td>
            <td>${doc.updatedBy || ""}</td>
            <td>${doc.updatedAt || ""}</td>
            <td class="action-buttons">
                ${
                    doc.fileName.endsWith(".docx") || doc.fileName.endsWith(".pdf")
                        ?
                          `<button class="btn btn-sm btn-secondary" onclick="openDocument(${doc.id})">🧾</button>
                           <button class="btn btn-sm btn-secondary" onclick="downloadDocument(${doc.id}, '${doc.fileName}')">🔽</button>
                           <button class="btn btn-sm btn-secondary" onclick="deleteDocument(${doc.id}, this)">❌</button>`
                        :
                          `<button class="btn btn-sm btn-secondary" onclick="downloadDocument(${doc.id}, '${doc.fileName}')">🔽</button>
                           <button class="btn btn-sm btn-secondary" onclick="deleteDocument(${doc.id}, this)">❌</button>`
                }
            </td>
            `;
        }
    });
}

function openDocument(id) {
    console.log(`Запрос открытия документа с ID: ${id}`);

    const viewerSpace = document.getElementById("viewerSpace"); // Основное пространство для просмотра
    const docxViewer = document.getElementById("docxViewer"); // Элемент для отображения .docx контента
    const documentViewer = document.getElementById("documentViewer"); // Элемент для отображения PDF
    const workspace = document.querySelector(".workspace"); // Контент внутри рабочей области

    if (!viewerSpace || !docxViewer || !documentViewer || !workspace) {
        console.error("Один или несколько элементов DOM не найдены!");
        alert("Не удалось открыть документ. Ошибка конфигурации страницы.");
        return;
    }

    // Переход в режим просмотра документа, скрываем рабочую область
    viewerSpace.style.display = "flex";
    workspace.style.display = "none";

    fetch(`/api/v1/docs/open/${id}`)
        .then((response) => {
        if (!response.ok) {
            throw new Error("Ошибка загрузки документа");
        }
        return response.blob();
    })
        .then((blob) => {
        const fileType = blob.type;

        if (fileType === "application/pdf") {
            // Отображение PDF
            documentViewer.style.display = "block";
            docxViewer.style.display = "none";
            documentViewer.src = URL.createObjectURL(blob);
        } else if (fileType === "application/vnd.openxmlformats-officedocument.wordprocessingml.document") {
            // Отображение DOCX через Mammoth.js
            documentViewer.style.display = "none";
            docxViewer.style.display = "block";

            const reader = new FileReader();
            reader.onload = function (event) {
                const arrayBuffer = event.target.result;
                mammoth.extractRawText({ arrayBuffer: arrayBuffer })
                    .then((result) => {
                    docxViewer.innerHTML = `<div class="docx-content">${result.value}</div>`; // Вставляем содержимое
                })
                    .catch((error) => {
                    console.error("Ошибка при преобразовании документа с Mammoth.js:", error);
                    alert("Не удалось обработать документ");
                });
            };
            reader.readAsArrayBuffer(blob);
        } else {
            alert("Неподдерживаемый формат файла");
            closeViewer();
        }
    })
        .catch((error) => {
        console.error("Ошибка при открытии документа:", error);
        alert("Не удалось загрузить документ.");
    });
}

function closeViewer() {
    const viewerSpace = document.getElementById("viewerSpace");
    const documentViewer = document.getElementById("documentViewer");
    const workspace = document.querySelector(".workspace");

    viewerSpace.style.display = "none";
    workspace.style.display = "block"; // Показываем основное рабочее пространство

    // Убираем src из <embed> (чтобы предотвратить повторные запросы)
    documentViewer.src = "";
}

function downloadDocument(id, fileName) {
    fetch(`/api/v1/docs/${id}`, {
        method: 'GET',
        credentials: 'same-origin', // Важно для передачи куки
    })
        .then((response) => {
        if (!response.ok) {
            throw new Error("Ошибка при загрузке файла.");
        }
        return response.blob();
    })
        .then((blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = fileName;
        a.click();
        URL.revokeObjectURL(url); // Освобождаем объект URL из памяти
    })
        .catch((error) => {
        console.error("Ошибка при загрузке документа:", error);
        alert("Не удалось скачать документ.");
    });
}

function deleteDocument(id, button) {
    if (!confirm("Вы действительно хотите удалить этот документ?")) {
        return;
    }

    fetch(`/api/v1/docs/${id}`, {
        method: 'DELETE',
        credentials: 'same-origin', // Передача куки
    })
        .then((response) => {
        if (!response.ok) {
            throw new Error("Ошибка при удалении документа.");
        }
        console.log(`Документ с ID ${id} успешно удалён.`);

        // Деактивируем кнопки в соответствующей строке
        const row = button.closest("tr");
        if (row) {
            const buttons = row.querySelectorAll("button");
            buttons.forEach((btn) => {
                btn.setAttribute("disabled", "true"); // Деактивируем кнопки
            });

            // Добавляем маркер для последующей проверки на удаление
            row.classList.add("marked-for-deletion");
        }
    })
        .catch((error) => {
        console.error("Ошибка при удалении документа:", error);
        alert("Не удалось удалить документ.");
    });
}

function formatDateToBackend(date) {
    const [year, month, day] = date.split("-");
    return `${day}.${month}.${year}`;
}

function logout() {
    if (!confirm("Вы действительно хотите выйти из системы?")) {
        return;
    }

    fetch('/perform-logout', {
        method: 'POST',
        credentials: 'same-origin', // Для отправки куки
    })
        .then((response) => {
        if (response.ok) {
            console.log("Logout успешно выполнен.");
            window.location.href = '/'; // Перенаправляем на главную страницу
        } else {
            throw new Error('Ошибка при попытке выхода.');
        }
    })
        .catch((error) => {
        console.error("Ошибка при выполнении logout:", error);
        alert("Произошла ошибка при попытке выхода. Пожалуйста, попробуйте снова.");
    });
}