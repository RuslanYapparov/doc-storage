const sortParams = {
    sortBy: "date",
    order: "desc",
    from: 0,
    size: 10,
    withOwned: false,
    withSharedForAll: true,
};

function loadMore(reset = false) {
    const grid = document.getElementById("documentGrid");

    // Удаляем строки, которые помечены как "удалённые"
    const markedRows = grid.querySelectorAll(".marked-for-deletion");
    markedRows.forEach((row) => row.remove());

    if (reset) {
        // Если reset=true, очищаем таблицу полностью
        sortParams.from = 0;
        grid.innerHTML = "";
    } else {
        sortParams.from = grid.rows.length; // Обновляем позицию "from"
    }

    const params = new URLSearchParams({
        sortBy: sortParams.sortBy,
        order: sortParams.order,
        from: sortParams.from,
        size: sortParams.size,
        withOwned: sortParams.withOwned,
        withSharedForAll: sortParams.withSharedForAll,
    });

    fetch(`/api/v1/docs/available?${params.toString()}`)
        .then((response) => {
        if (!response.ok) {
            throw new Error("Ошибка загрузки данных");
        }
        return response.json();
    })
        .then((data) => {
        if (data.length === 0) {
            console.log("Больше нет данных для загрузки.");
            return;
        }
        console.log("Данные получены: ", data);
        addDocumentsToGrid(data);
    })
        .catch((error) => {
        console.error("Ошибка при загрузке данных:", error);
    });
}

function applySort() {
    const grid = document.getElementById("documentGrid");

    sortParams.sortBy = document.getElementById("sortBy").value;
    sortParams.order = document.getElementById("order").value;
    sortParams.size = parseInt(document.getElementById("size").value, 10);
    sortParams.withOwned = document.getElementById("withOwned").checked;
    sortParams.withSharedForAll = document.getElementById("withSharedForAll").checked;

    const searchMenu = document.getElementById("searchMenu");
    if (searchMenu.style.display !== 'none') {
        search();
    } else {
        loadMore(true);
    }
}

function search() {
    if (event) event.preventDefault();

    const grid = document.getElementById("documentGrid");
    const searchPattern = document.getElementById("searchPattern").value.trim();
    const dateFrom = document.getElementById("dateFrom").value;
    const dateTo = document.getElementById("dateTo").value;

    console.log("Начало обработки запроса поиска");

    // Если это первый поиск, сбрасываем таблицу
    if (isFirstSearch) {
        sortParams.from = 0;
        grid.innerHTML = ""; // Очищаем таблицу
        isFirstSearch = false;
    } else {
        sortParams.from = grid.rows.length;
    }

    sortParams.sortBy = document.getElementById("sortBy").value;
    sortParams.order = document.getElementById("order").value;
    sortParams.size = parseInt(document.getElementById("size").value, 10) || 10;

    const params = new URLSearchParams({
        searchFor: searchPattern,
        sortBy: sortParams.sortBy,
        order: sortParams.order,
        from: sortParams.from,
        size: sortParams.size,
        withOwned: sortParams.withOwned,
        withSharedForAll: sortParams.withSharedForAll
    });
    if (dateFrom) {
        params.append("since", formatDateToBackend(dateFrom));
    }
    if (dateTo) {
        params.append("until", formatDateToBackend(dateTo));
    }

    fetch(`/api/v1/docs/search/available?${params.toString()}`)
        .then((response) => {
        if (!response.ok) {
            throw new Error("Ошибка выполнения поиска документов");
        }
        return response.json();
    })
        .then((data) => {
        if (data.length === 0) {
            console.log("Дополнительные документы не найдены.");
            return;
        }
        console.log("Данные получены: ", data);
        addDocumentsToGrid(data);
    })
        .catch((error) => {
        console.error("Ошибка при обработке запроса:", error);
    });
}