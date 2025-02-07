document.addEventListener('DOMContentLoaded', function() {
    // Add event listeners or any JavaScript-related functionality here
});

// Example AJAX for upload form
document.getElementById('uploadForm').addEventListener('submit', function(event) {
    event.preventDefault();
    let formData = new FormData();
    formData.append("file", document.getElementById("file").files[0]);
    formData.append("title", document.getElementById("title").value);
    formData.append("description", document.getElementById("description").value);

    fetch('/api/v1/docs/uploading', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        console.log('Success:', data);
    })
    .catch(error => {
        console.error('Error:', error);
    });
});
