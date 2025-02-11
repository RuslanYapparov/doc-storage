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

function toggleGeneralAccess() {
    document.querySelector(".access-options").innerHTML = `
    <h5>Общий доступ</h5>
    <div class="d-flex flex-column">
      <div class="btn-group" style="margin-bottom: 15px;">
        <button class="btn btn-primary" onclick="openGeneralAccess()">Открыть</button>
        <button class="btn btn-danger" onclick="closeGeneralAccess()">Закрыть</button>
      </div>
      <div class="form-check mt-3">
        <input type="radio" class="form-check-input" name="generalAccess" id="readOnly" value="READ_ONLY" checked>
        <label class="form-check-label" for="readOnly">Только для чтения</label>
      </div>
      <div class="form-check">
        <input type="radio" class="form-check-input" name="generalAccess" id="edit" value="EDIT">
        <label class="form-check-label" for="edit">Изменение</label>
      </div>
      <div class="form-check">
        <input type="radio" class="form-check-input" name="generalAccess" id="remove" value="REMOVE">
        <label class="form-check-label" for="remove">Удаление</label>
      </div>
    </div>
  `;
    clearError();
    clearSuccessNotification();
}

function togglePrivateAccess() {
    document.querySelector(".access-options").innerHTML = `
    <h5>Личный доступ</h5>
  <div class="d-flex flex-column">
    <input type="text" id="usernameInput" class="form-control mb-3" placeholder="Введите никнейм пользователя">
    <div class="btn-group" style="width: 100%;">
      <button class="btn btn-primary" style="flex-grow: 1; margin-bottom: 7px;" onclick="grantPrivateAccess()">Предоставить</button>
      <button class="btn btn-danger" style="flex-grow: 1; margin-bottom: 7px;" onclick="revokePrivateAccess()">Отозвать</button>
    </div>
    <div class="form-check mt-3">
      <input type="radio" class="form-check-input" name="privateAccess" id="readOnlyPrivate" value="READ_ONLY" checked>
      <label class="form-check-label" for="readOnlyPrivate">Только для чтения</label>
    </div>
    <div class="form-check">
      <input type="radio" class="form-check-input" name="privateAccess" id="editPrivate" value="EDIT">
      <label class="form-check-label" for="editPrivate">Изменение</label>
    </div>
    <div class="form-check">
      <input type="radio" class="form-check-input" name="privateAccess" id="removePrivate" value="REMOVE">
      <label class="form-check-label" for="removePrivate">Удаление</label>
    </div>
  </div>
  `;
    clearError();
    clearSuccessNotification();
}

function toggleUsersWithAccess() {
    document.querySelector(".access-options").innerHTML = `
    <h5>Пользователи с личным доступом</h5>
    <div class="form-check mt-3">
      <input type="radio" class="form-check-input" name="accessType" id="readOnlyAccess" value="READ_ONLY" checked>
      <label class="form-check-label" for="readOnlyAccess">Только для чтения</label>
    </div>
    <div class="form-check">
      <input type="radio" class="form-check-input" name="accessType" id="editAccess" value="EDIT">
      <label class="form-check-label" for="editAccess">Изменение</label>
    </div>
    <div class="form-check">
      <input type="radio" class="form-check-input" name="accessType" id="removeAccess" value="REMOVE">
      <label class="form-check-label" for="removeAccess">Удаление</label>
    </div>
    <textarea class="form-control mt-3" id="usernamesField" readonly style="height: 100px;">Здесь будут отображаться никнеймы...</textarea>
  `;

    const radios = document.querySelectorAll('input[name="accessType"]');
    radios.forEach((radio) => {
        radio.addEventListener("change", fetchUsernamesWithAccess);
    });

    clearError();
    clearSuccessNotification();
    fetchUsernamesWithAccess();
}


function addDocumentsToGrid(docs) {
    const grid = document.getElementById("documentGrid");
    docs.forEach((doc) => {
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
                <button class="btn btn-sm btn-secondary"
                      onclick="openAccessWorkspace(
                              ${doc.id},
                              '${doc.title || ""}',
                              '${doc.description || ""}',
                              '${doc.ownerName || ""}',
                              '${doc.fileName || ""}',
                              '${doc.createdAt || ""}',
                              '${doc.updatedAt || ""}',
                              '${doc.updatedBy || ""}',
                              '${doc.commonAccessType || ""}')">⏏️</button>
            </td>
            `;
        }
    });
}

function openAccessWorkspace(id, title, description, owner, fileName, createdAt, updatedAt, updatedBy, commonAccessType) {
    document.querySelector(".workspace").style.display = "none";
    document.querySelector(".access-workspace").style.display = "block";

    clearError();
    clearSuccessNotification();
    const accessOptionsContainer = document.querySelector(".access-options");
    if (accessOptionsContainer) {
        accessOptionsContainer.innerHTML = "";
    }

    const docTitleElem = document.getElementById('accessDocTitle');
    docTitleElem.innerText = title;
    docTitleElem.dataset.id = id;
    document.getElementById('accessDocTitle').innerText = title;
    document.getElementById('accessDocDescription').innerText = description;
    document.getElementById('accessDocOwner').innerText = owner;
    document.getElementById('accessDocFile').innerText = fileName;
    document.getElementById('accessDocCreated').innerText = createdAt;
    document.getElementById('accessDocUpdated').innerText = updatedAt;
    document.getElementById('accessDocUpdatedBy').innerText = updatedBy;
    document.getElementById('accessDocAccessType').innerText = commonAccessType || "Закрыт";

    const generalReadOnlyRadio = document.querySelector('input[name="generalAccess"][value="READ_ONLY"]');
    if (generalReadOnlyRadio) {
        generalReadOnlyRadio.checked = true;
    }

    const privateReadOnlyRadio = document.querySelector('input[name="privateAccess"][value="READ_ONLY"]');
    if (privateReadOnlyRadio) {
        privateReadOnlyRadio.checked = true;
    }
}

function openGeneralAccess() {
    const documentId = document.getElementById('accessDocTitle').dataset.id;
    const accessType = document.querySelector('input[name="generalAccess"]:checked').value;
    clearError();
    if (!accessType) {
        showError("Выберите тип общего доступа.");
        return;
    }

    fetch(`/api/v1/docs/share/open/${documentId}?accessType=${accessType}`, {
        method: 'PATCH',
        headers: {
            'Content-Type': 'application/json',
        },
    })
        .then((response) => {
        if (!response.ok) {
            return response.json().then((error) => {
                throw new Error(error.message || "Ошибка открытия общего доступа.");
            });
        }
        return response.json();
    })
        .then((documentDto) => {
        showSuccessNotification(`Общий доступ открыт: ${accessType}`);
        document.getElementById('accessDocAccessType').textContent = accessType;
    })
        .catch((error) => {
        showError(error.message);
    });
}

function closeGeneralAccess() {
    const documentId = document.getElementById('accessDocTitle').dataset.id;
    clearError();

    fetch(`/api/v1/docs/share/close/${documentId}`, {
        method: 'PATCH',
        headers: {
            'Content-Type': 'application/json',
        },
    })
        .then((response) => {
        if (!response.ok) {
            return response.json().then((error) => {
                throw new Error(error.message || "Ошибка закрытия общего доступа.");
            });
        }
        return response.json();
    })
        .then(() => {
        showSuccessNotification("Общий доступ закрыт.");
        document.getElementById('accessDocAccessType').textContent = "Закрыт";
    })
        .catch((error) => {
        showError(error.message);
    });
}

function grantPrivateAccess() {
    const documentId = document.getElementById('accessDocTitle').dataset.id;
    const username = document.getElementById('usernameInput').value.trim();
    const accessType = document.querySelector('input[name="privateAccess"]:checked').value;

    clearError();

    if (!username) {
        showError("Введите никнейм пользователя.");
        return;
    }

    const accessData = {
        docId: documentId,
        username: username,
        accessType: accessType,
    };

    fetch(`/api/v1/accesses`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(accessData),
    })
        .then((response) => {
        if (!response.ok) {
            return response.json().then((error) => {
                throw new Error(error.message || "Ошибка предоставления доступа.");
            });
        }
        return response.json();
    })
        .then((docUserAccessDto) => {
        showSuccessNotification(`Для пользователя '${username}' установлен доступ: ${accessType}.`);
    })
        .catch((error) => {
        showError(error.message);
    });
}

function revokePrivateAccess() {
    const documentId = document.getElementById('accessDocTitle').dataset.id;
    const username = document.getElementById('usernameInput').value.trim();
    clearError();
    if (!username) {
        showError("Введите никнейм пользователя.");
        return;
    }

    fetch(`/api/v1/accesses?docId=${documentId}&username=${username}`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
        },
    })
        .then((response) => {
        if (!response.ok) {
            return response.json().then((error) => {
                throw new Error(error.message || "Ошибка отзыва доступа.");
            });
        }
        return Promise.resolve();
    })
        .then(() => {
        showSuccessNotification(`Доступ для пользователя '${username}' отозван.`);
    })
        .catch((error) => {
        showError(error.message);
    });
}

function fetchUsernamesWithAccess() {
    const documentId = document.getElementById('accessDocTitle').dataset.id;
    const accessType = document.querySelector('input[name="accessType"]:checked').value;
    clearError();

    fetch(`/api/v1/accesses/${documentId}?accessType=${accessType}`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        },
    })
        .then((response) => {
        if (!response.ok) {
            return response.json().then((error) => {
                throw new Error(error.message || "Ошибка получения списка никнеймов.");
            });
        }
        return response.json();
    })
        .then((usernames) => {
        const usernamesField = document.getElementById('usernamesField');
        if (usernames.length > 0) {
            usernamesField.value = usernames.join('\n');
        } else {
            usernamesField.value = "Нет пользователей с выбранным доступом.";
        }
    })
        .catch((error) => {
        showError(error.message);
    });
}

function showError(message) {
    const errorBlock = document.getElementById('errorMessage');
    errorBlock.textContent = message;
    errorBlock.style.display = 'block';
}

function clearError() {
    const errorBlock = document.getElementById('errorMessage');
    errorBlock.textContent = '';
    errorBlock.style.display = 'none';
}

function showSuccessNotification(message) {
    const successNotification = document.getElementById('successNotification');
    successNotification.textContent = message;
    successNotification.style.display = 'block';

    setTimeout(() => {
        clearSuccessNotification();
    }, 5000);
}

function clearSuccessNotification() {
    const successNotification = document.getElementById('successNotification');
    successNotification.textContent = '';
    successNotification.style.display = 'none';
}

function goBack() {
    document.querySelector(".workspace").style.display = "block";
    document.querySelector(".access-workspace").style.display = "none";
}