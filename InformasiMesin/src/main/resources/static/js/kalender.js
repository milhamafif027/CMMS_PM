document.addEventListener("DOMContentLoaded", function () {
  const API_BASE_URL = "http://localhost:8080/api";
  const calendarEl = document.getElementById("calendar");
  const modal = document.getElementById("event-modal");
  const overlay = document.querySelector(".modal-overlay");
  const saveButton = document.getElementById("save-event");
  const closeBtn = document.getElementById("close-modal");
  const scheduleForm = document.getElementById("schedule-form");
  const rescheduleForm = document.getElementById("reschedule-form");
  const scheduleTypeRadios = document.querySelectorAll(
    'input[name="schedule-type"]'
  );
  let selectedDate = null;

  // Inisialisasi kalender
  const calendar = new FullCalendar.Calendar(calendarEl, {
    initialView: "dayGridMonth",
    headerToolbar: {
      left: "prev,next today",
      center: "title",
      right: "dayGridMonth,timeGridWeek,timeGridDay",
    },
    selectable: true,
    dateClick: function (info) {
      selectedDate = info.dateStr;
      openModal();
    },
    eventClick: function (info) {
      alert(`
        Entitas: ${info.event.extendedProps.entityName}
        Jenis: ${info.event.extendedProps.action}
        Teknisi: ${info.event.extendedProps.technician}
        Deskripsi: ${info.event.extendedProps.description}
      `);
    },
    events: async function (fetchInfo, successCallback, failureCallback) {
      try {
        // Menggunakan endpoint yang benar dan menambahkan parameter mesinId dan year
        const mesinId = 2; // Ganti dengan ID mesin yang sesuai atau ambil dari selector
        const year = fetchInfo.start.getFullYear();

        const response = await fetch(
          `${API_BASE_URL}/maintenance/mesin/${mesinId}/year/${year}`
        );
        const data = await response.json();

        // Pastikan data adalah array sebelum menggunakan map
        if (!Array.isArray(data)) {
          console.error("Data bukan array:", data);
          successCallback([]);
          return;
        }

        const events = data.map((event) => ({
          id: event.id,
          title: `${event.mesin.entityName} - ${event.action}`,
          start: `${event.tahun}-${getMonthNumber(event.bulan)}-${
            event.tanggal
          }`,
          allDay: true,
          extendedProps: {
            entityNo: event.mesin.entityNo,
            entityName: event.mesin.entityName,
            action: event.action,
            description: event.description,
            technician: event.technician,
            isRescheduled: event.isRescheduled,
            rescheduleReason: event.rescheduleReason,
            status: event.status,
          },
          backgroundColor: event.isRescheduled ? "#e74c3c" : "#2ecc71",
        }));
        successCallback(events);
      } catch (error) {
        console.error("Error fetching events:", error);
        failureCallback(error);
      }
    },
  });

  // Helper function untuk konversi nama bulan ke angka
  function getMonthNumber(monthName) {
    const months = {
      Januari: "01",
      Februari: "02",
      Maret: "03",
      April: "04",
      Mei: "05",
      Juni: "06",
      Juli: "07",
      Agustus: "08",
      September: "09",
      Oktober: "10",
      November: "11",
      Desember: "12",
    };
    return months[monthName] || "01";
  }

  // Helper function untuk konversi angka bulan ke nama bulan
  function getMonthName(monthNumber) {
    const monthNames = [
      "Januari",
      "Februari",
      "Maret",
      "April",
      "Mei",
      "Juni",
      "Juli",
      "Agustus",
      "September",
      "Oktober",
      "November",
      "Desember",
    ];
    return monthNames[monthNumber - 1] || "Januari";
  }

  calendar.render();

  // Fungsi untuk membuka modal
  function openModal() {
    modal.style.display = "block";
    overlay.style.display = "block";
  }

  // Fungsi untuk menutup modal
  function closeModal() {
    modal.style.display = "none";
    overlay.style.display = "none";
    resetForm();
  }

  // Fungsi untuk mereset form
  function resetForm() {
    document.getElementById("entity-number").value = "";
    document.getElementById("entity-name").value = "";
    document.getElementById("maintenance-type").value = "";
    document.getElementById("description").value = "";
    document.getElementById("technician").value = "";
    document.getElementById("reschedule-entity-number").value = "";
    document.getElementById("reschedule-entity-name").value = "";
    document.getElementById("reschedule-reason").value = "";
    document.getElementById("reschedule-maintenance-type").value = "";
    document.getElementById("reschedule-technician").value = "";
  }

  // Toggle form berdasarkan pilihan Schedule atau Reschedule
  scheduleTypeRadios.forEach((radio) => {
    radio.addEventListener("change", (e) => {
      if (e.target.value === "schedule") {
        scheduleForm.style.display = "block";
        rescheduleForm.style.display = "none";
      } else {
        scheduleForm.style.display = "none";
        rescheduleForm.style.display = "block";
      }
    });
  });

  // Fungsi untuk mengisi nama entitas secara otomatis
  document
    .getElementById("entity-number")
    .addEventListener("input", async (e) => {
      const entityNo = e.target.value;
      if (entityNo) {
        try {
          const response = await fetch(
            `${API_BASE_URL}/maintenance/mesin/by-number/${entityNo}`
          );
          if (!response.ok) {
            throw new Error("Mesin tidak ditemukan");
          }
          const mesin = await response.json();
          document.getElementById("entity-name").value = mesin.entityName;
          document.getElementById("mesin-id").value = mesin.id;
        } catch (error) {
          console.error("Error fetching mesin:", error);
          document.getElementById("entity-name").value = "";
          document.getElementById("mesin-id").value = "";
          alert("Mesin tidak ditemukan. Pastikan nomor entitas benar.");
        }
      } else {
        document.getElementById("entity-name").value = "";
        document.getElementById("mesin-id").value = "";
      }
    });

  document
    .getElementById("reschedule-entity-number")
    .addEventListener("input", async (e) => {
      const entityNo = e.target.value;
      if (entityNo) {
        try {
          const response = await fetch(
            `${API_BASE_URL}/maintenance/mesin/by-number/${entityNo}`
          );
          if (!response.ok) {
            throw new Error("Mesin tidak ditemukan");
          }
          const mesin = await response.json();
          document.getElementById("reschedule-entity-name").value =
            mesin.entityName;
          document.getElementById("reschedule-mesin-id").value = mesin.id;
        } catch (error) {
          console.error("Error fetching mesin:", error);
          document.getElementById("reschedule-entity-name").value = "";
          document.getElementById("reschedule-mesin-id").value = "";
        }
      } else {
        document.getElementById("reschedule-entity-name").value = "";
        document.getElementById("reschedule-mesin-id").value = "";
      }
    });

  // Fungsi menyimpan event
  async function handleSaveEvent() {
    const selectedType = document.querySelector(
      'input[name="schedule-type"]:checked'
    ).value;

    try {
      if (selectedType === "schedule") {
        // Logika untuk Schedule
        const mesinId = document.getElementById("mesin-id").value;
        const entityNo = document.getElementById("entity-number").value;
        const maintenanceType =
          document.getElementById("maintenance-type").value;
        const description = document.getElementById("description").value;
        const technician = document.getElementById("technician").value;

        if (!entityNo || !maintenanceType || !technician) {
          alert("Semua field harus diisi!");
          return;
        }

        // Ekstrak tahun, bulan, dan tanggal dari selectedDate
        const dateParts = selectedDate.split("-");
        const year = parseInt(dateParts[0]);
        const month = parseInt(dateParts[1]);
        const day = parseInt(dateParts[2]);

        // Konversi angka bulan ke nama bulan
        const monthName = getMonthName(month);

        const response = await fetch(`${API_BASE_URL}/maintenance`, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            mesin: {
              id: mesinId,
            },
            bulan: monthName,
            tanggal: day,
            tahun: year,
            status: "Scheduled",
            action: maintenanceType,
            description: description,
            technician: technician,
            isRescheduled: false,
          }),
        });

        if (!response.ok) {
          throw new Error("Gagal menyimpan jadwal");
        }

        calendar.refetchEvents(); // Refresh kalender
      } else {
        // Logika untuk Reschedule
        const mesinId = document.getElementById("reschedule-mesin-id").value;
        const entityNo = document.getElementById(
          "reschedule-entity-number"
        ).value;
        const reason = document.getElementById("reschedule-reason").value;
        const maintenanceType = document.getElementById(
          "reschedule-maintenance-type"
        ).value;
        const technician = document.getElementById(
          "reschedule-technician"
        ).value;

        if (!entityNo || !reason || !maintenanceType || !technician) {
          alert("Semua field harus diisi!");
          return;
        }

        // Ekstrak tahun, bulan, dan tanggal dari selectedDate
        const dateParts = selectedDate.split("-");
        const year = parseInt(dateParts[0]);
        const month = parseInt(dateParts[1]);
        const day = parseInt(dateParts[2]);

        // Konversi angka bulan ke nama bulan
        const monthName = getMonthName(month);

        const response = await fetch(`${API_BASE_URL}/maintenance`, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            mesin: {
              id: mesinId,
            },
            bulan: monthName,
            tanggal: day,
            tahun: year,
            status: "Rescheduled",
            action: maintenanceType,
            description: reason,
            technician: technician,
            isRescheduled: true,
            rescheduleReason: reason,
          }),
        });

        if (!response.ok) {
          throw new Error("Gagal menyimpan reschedule");
        }

        calendar.refetchEvents(); // Refresh kalender
      }

      closeModal();
    } catch (error) {
      console.error("Error saving event:", error);
      alert(error.message || "Terjadi kesalahan saat menyimpan data.");
    }
  }

  // Event Listeners
  saveButton.addEventListener("click", handleSaveEvent);
  overlay.addEventListener("click", closeModal);
  closeBtn.addEventListener("click", closeModal);
});
