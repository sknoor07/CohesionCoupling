document.addEventListener('DOMContentLoaded', () => {
    const dropZone = document.getElementById('drop-zone');
    const fileInput = document.getElementById('file-input');
    const dropZoneText = document.getElementById('drop-zone-text');
    const uploadForm = document.getElementById('upload-form');

    dropZone.addEventListener('click', () => {
        fileInput.click();
    });

    dropZone.addEventListener('dragover', (e) => {
        e.preventDefault();
        dropZone.classList.add('dragging');
    });

    dropZone.addEventListener('dragleave', () => {
        dropZone.classList.remove('dragging');
    });

    dropZone.addEventListener('drop', (e) => {
        e.preventDefault();
        dropZone.classList.remove('dragging');

        if (e.dataTransfer.files.length) {
            fileInput.files = e.dataTransfer.files;
            const fileNames = Array.from(e.dataTransfer.files).map(file => file.name).join(', ');
            dropZoneText.textContent = fileNames;
        }
    });

    fileInput.addEventListener('change', () => {
        const fileNames = Array.from(fileInput.files).map(file => file.name).join(', ');
        dropZoneText.textContent = fileNames;
    });

    uploadForm.addEventListener('submit', (e) => {
        if (!fileInput.files.length) {
            e.preventDefault();
            alert("Please select or drop Java files before analyzing.");
        }
    });
});
