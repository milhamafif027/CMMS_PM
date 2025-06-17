document.addEventListener("DOMContentLoaded", function () {
  const logoutButton = document.querySelector(".btn-danger");

  if (logoutButton) {
    logoutButton.addEventListener("click", function () {
      window.location.href = "/";
    });
  }
});
