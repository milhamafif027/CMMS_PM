document.addEventListener("DOMContentLoaded", function () {
  const btnStart = document.querySelector(".btn-start");

  if (btnStart) {
    btnStart.addEventListener("click", function () {
      redirectToMesin();
    });
  }
});

function redirectToMesin() {
  window.location.href = "/mesin"; 
}
