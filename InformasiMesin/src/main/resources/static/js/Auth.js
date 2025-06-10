document.addEventListener("DOMContentLoaded", function () {
  const API_BASE_URL = window.location.origin + "/api";
  const loginForm = document.getElementById("loginForm");

  if (loginForm) {
    loginForm.addEventListener("submit", async function (e) {
      e.preventDefault();

      const formData = new URLSearchParams();
      formData.append("username", document.getElementById("username").value);
      formData.append("password", document.getElementById("password").value);

      try {
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
          method: "POST",
          headers: {
            "Content-Type": "application/x-www-form-urlencoded",
          },
          credentials: "include",
          body: formData,
        });

        if (response.ok) {
          window.location.href = "/index.html"; // Redirect jika sukses
        } else {
          showError("Login gagal! Periksa username dan password.");
        }
      } catch (error) {
        console.error("Login error:", error);
        showError("Terjadi kesalahan saat login.");
      }
    });
  }

  // Logout handler
  const logoutButton = document.querySelector(".btn-logout");
  if (logoutButton) {
    logoutButton.addEventListener("click", async function () {
      try {
        const response = await fetch(`${API_BASE_URL}/auth/logout`, {
          method: "POST",
          credentials: "include",
        });

        if (response.ok) {
          window.location.href = "/login.html"; // Redirect ke halaman login
        } else {
          alert("Gagal logout, coba lagi.");
        }
      } catch (error) {
        console.error("Logout error:", error);
        alert("Terjadi kesalahan saat logout.");
      }
    });
  }

  function showError(message) {
    const errorElement = document.querySelector(".error-message");
    if (errorElement) {
      errorElement.textContent = message;
      errorElement.style.display = "block";
    } else {
      alert(message);
    }
  }
});
