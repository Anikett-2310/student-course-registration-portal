/**
 * validation.js
 * Client-Side JavaScript Validations using Regular Expressions
 * Web Technologies Laboratory - Experiment 3 & 4 Concept
 */

/**
 * Validates Student Login Form
 * @returns {boolean} true if valid, false otherwise
 */
function validateStudentLogin() {
    var studentIdInput = document.getElementById("studentId");
    var passwordInput = document.getElementById("password");
    var errorDiv = document.getElementById("clientError");

    // Clear previous client error
    if (errorDiv) {
        errorDiv.style.display = "none";
        errorDiv.innerText = "";
    }

    var studentId = studentIdInput.value.trim();
    var password = passwordInput.value.trim();

    // 1. Empty Check
    if (studentId === "") {
        showError("Please enter your Student ID.", studentIdInput);
        return false;
    }

    // 2. Regular Expression for Student ID
    // College Roll No pattern: supports all 4-year cohorts (e.g. 26881A1201, 25881A1201, 24881A1201, 23881A1201)
    var studentIdPattern = /^(\d{2}881A[0-9A-Za-z]{4}|[0-9]{2}[0-9A-Za-z]{8})$/i;
    if (!studentIdPattern.test(studentId)) {
        showError("Student ID format is invalid (e.g., 26881A1201, 25881A1201, 24881A1201, 23881A1201).", studentIdInput);
        return false;
    }

    // 3. Password Check
    if (password === "") {
        showError("Please enter your password.", passwordInput);
        return false;
    }

    return true;
}

/**
 * Displays error message in the client error container
 */
function showError(message, focusElement) {
    var errorDiv = document.getElementById("clientError");
    if (errorDiv) {
        errorDiv.innerText = message;
        errorDiv.style.display = "block";
    } else {
        alert(message);
    }
    if (focusElement) {
        focusElement.focus();
    }
}

/**
 * Reads URL parameters on page load to display feedback alerts
 * (e.g., ?error=invalid_credentials or ?msg=logged_out)
 */
window.addEventListener("DOMContentLoaded", function () {
    var urlParams = new URLSearchParams(window.location.search);
    var statusDiv = document.getElementById("serverStatus");

    if (!statusDiv) return;

    if (urlParams.has("error")) {
        var error = urlParams.get("error");
        statusDiv.className = "alert alert-error";
        if (error === "invalid_credentials") {
            statusDiv.innerText = "Invalid Student ID or Password. Please try again.";
        } else if (error === "invalid_format") {
            statusDiv.innerText = "Student ID format is invalid (e.g., 26881A1201, 25881A1201, 24881A1201, 23881A1201).";
        } else if (error === "session_expired") {
            statusDiv.innerText = "Your session has expired. Please log in again.";
        } else if (error === "db_error") {
            statusDiv.innerText = "Database connection error. Please verify database is running.";
        } else {
            statusDiv.innerText = "An error occurred. Please try again.";
        }
        statusDiv.style.display = "block";
    } else if (urlParams.has("msg")) {
        var msg = urlParams.get("msg");
        statusDiv.className = "alert alert-success";
        if (msg === "logged_out") {
            statusDiv.innerText = "You have been logged out successfully.";
        }
        statusDiv.style.display = "block";
    }
});
