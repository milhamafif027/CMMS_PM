/**
 * @fileoverview Main application for maintenance scheduling system
 * @version 1.0.0
 * @description Handles calendar display, maintenance scheduling, and reporting
 */

document.addEventListener("DOMContentLoaded", function () {
  // ========================================================
  // Configuration Constants
  // ========================================================
  const API_BASE_URL = "http://localhost:8080/api";
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
  const monthNames = Object.keys(months);

  // ========================================================
  // DOM Elements
  // ========================================================
  const elements = {
    calendar: document.getElementById("calendar"),
    modal: document.getElementById("event-modal"),
    overlay: document.querySelector(".modal-overlay"),
    saveButton: document.getElementById("save-event"),
    closeBtn: document.getElementById("close-modal"),
    rescheduleForm: document.getElementById("reschedule-form"),
    rescheduleEntityNumber: document.getElementById("reschedule-entity-number"),
    rescheduleEntityName: document.getElementById("reschedule-entity-name"),
    rescheduleId: document.getElementById("reschedule-mesin-id"),
    printReportButton: document.getElementById("print-report"),
  };

  let selectedDate = null;

  // ========================================================
  // Utility Functions
  // ========================================================

  /**
   * Gets month number from month name
   * @param {string} monthName
   * @returns {string} Two-digit month number
   */
  function getMonthNumber(monthName) {
    return months[monthName] || "01";
  }

  /**
   * Gets month name from month number (1-12)
   * @param {number} monthNumber
   * @returns {string} Month name
   */
  function getMonthName(monthNumber) {
    return monthNames[monthNumber - 1] || "Januari";
  }

  /**
   * Parses date string into components
   * @param {string} dateStr - Format YYYY-MM-DD
   * @returns {Object} Parsed date components
   */
  function parseDateParts(dateStr) {
    const dateParts = dateStr.split("-");
    return {
      year: parseInt(dateParts[0]),
      month: parseInt(dateParts[1]),
      day: parseInt(dateParts[2]),
      monthName: getMonthName(parseInt(dateParts[1])),
    };
  }

  /**
   * Checks if date is today
   * @param {Date} date
   * @returns {boolean}
   */
  function isTodayDate(date) {
    const today = new Date();
    return (
      date.getDate() === today.getDate() &&
      date.getMonth() === today.getMonth() &&
      date.getFullYear() === today.getFullYear()
    );
  }

  // ========================================================
  // Modal Management
  // ========================================================

  function openModal() {
    elements.modal.style.display = "block";
    elements.overlay.style.display = "block";
  }

  function closeModal() {
    elements.modal.style.display = "none";
    elements.overlay.style.display = "none";
    resetForm();
  }

  function resetForm() {
    [
      "reschedule-entity-number",
      "reschedule-entity-name",
      "reschedule-reason",
      "reschedule-maintenance-type",
      "reschedule-technician",
    ].forEach((id) => {
      const element = document.getElementById(id);
      if (element) element.value = "";
    });
  }

  // ========================================================
  // API Interaction Functions
  // ========================================================

  /**
   * Fetches machine by entity number
   * @param {string} entityNo
   * @returns {Promise<Object|null>} Machine data
   */
  async function fetchMesinByNumber(entityNo) {
    try {
      const response = await fetch(
        `${API_BASE_URL}/maintenance/mesin/by-number/${entityNo}`
      );
      if (!response.ok) throw new Error("Mesin tidak ditemukan");
      return await response.json();
    } catch (error) {
      console.error("Error fetching mesin:", error);
      return null;
    }
  }

  /**
   * Populates machine dropdown
   */
  async function populateMesinDropdown() {
    try {
      const response = await fetch(`${API_BASE_URL}/mesin`);
      if (!response.ok) throw new Error("Gagal mengambil data mesin");
      const mesinList = await response.json();

      const dropdown = elements.rescheduleEntityNumber;
      if (!dropdown) return;

      dropdown.innerHTML = '<option value="">Pilih Nomor Entitas</option>';
      mesinList.forEach((mesin) => {
        const option = document.createElement("option");
        option.value = mesin.entityNo;
        option.text = `${mesin.entityNo} - ${mesin.entityName}`;
        dropdown.appendChild(option);
      });
    } catch (error) {
      console.error("Error fetching mesin list:", error);
    }
  }

  /**
   * Saves maintenance schedule
   * @returns {Promise<boolean>} Success status
   */
  async function saveSchedule() {
    try {
      const dateInfo = parseDateParts(selectedDate);
      const requiredFields = [
        elements.rescheduleEntityNumber?.value,
        document.getElementById("reschedule-reason")?.value,
        document.getElementById("reschedule-maintenance-type")?.value,
        document.getElementById("reschedule-technician")?.value,
      ];

      if (requiredFields.some((field) => !field)) {
        throw new Error("Semua field harus diisi!");
      }

      const requestData = {
        bulan: dateInfo.monthName,
        tanggal: dateInfo.day,
        tahun: dateInfo.year,
        isRescheduled: true,
        mesin: { id: elements.rescheduleId?.value },
        status: "Rescheduled",
        action: document.getElementById("reschedule-maintenance-type")?.value,
        description: document.getElementById("reschedule-reason")?.value,
        technician: document.getElementById("reschedule-technician")?.value,
        rescheduleReason: document.getElementById("reschedule-reason")?.value,
      };

      const response = await fetch(`${API_BASE_URL}/maintenance`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(requestData),
      });

      if (!response.ok) {
        throw new Error("Gagal menyimpan reschedule");
      }

      alert("Reschedule berhasil disimpan!");
      return true;
    } catch (error) {
      console.error("Save error:", error);
      alert(error.message || "Terjadi kesalahan saat menyimpan data.");
      return false;
    }
  }

  /**
   * Saves automatic schedule for given month/year
   * @param {number} year
   * @param {number} month
   */
  async function saveAutomaticSchedule(year, month) {
    try {
      const key = `automaticSchedule-${year}-${month}`;
      if (localStorage.getItem(key)) {
        console.log("Jadwal sudah digenerate sebelumnya.");
        return;
      }

      const response = await fetch(
        `${API_BASE_URL}/maintenance/automatic-schedule?year=${year}&month=${month}`
      );
      if (!response.ok) {
        throw new Error("Gagal mengenerate jadwal otomatis");
      }

      localStorage.setItem(key, "true");
    } catch (error) {
      console.error("Error generating schedule:", error);
    }
  }

  // ========================================================
  // Event Handlers
  // ========================================================

  async function handleEntityNumberChange(e, targetNameField, targetIdField) {
    const entityNo = e.target.value;
    const targetNameElement = document.getElementById(targetNameField);
    const targetIdElement = document.getElementById(targetIdField);

    if (!targetNameElement || !targetIdElement) return;

    if (entityNo) {
      try {
        const mesin = await fetchMesinByNumber(entityNo);
        if (mesin) {
          targetNameElement.value = mesin.entityName;
          targetIdElement.value = mesin.id;
        } else {
          throw new Error("Mesin tidak ditemukan");
        }
      } catch (error) {
        console.error("Error:", error);
        targetNameElement.value = "";
        targetIdElement.value = "";
        alert("Mesin tidak ditemukan. Pastikan nomor entitas benar.");
      }
    } else {
      targetNameElement.value = "";
      targetIdElement.value = "";
    }
  }

  // ========================================================
  // Calendar Initialization
  // ========================================================
  const calendar = new FullCalendar.Calendar(elements.calendar, {
    initialView: "dayGridMonth",
    headerToolbar: {
      left: "prev,next today",
      center: "title",
      right: "dayGridMonth,timeGridWeek,timeGridDay",
    },
    dayMaxEvents: false,
    eventDisplay: "block",
    eventTextColor: "#FFFFFF",
    selectable: true,
    dateClick: function (info) {
      selectedDate = info.dateStr;
      openModal();
    },
    eventClick: function (info) {
      const props = info.event.extendedProps;
      alert(`
        Entitas: ${props.entityName}
        Jenis: ${props.action}
        Teknisi: ${props.technician}
        Deskripsi: ${props.description || "-"}
        Status: ${props.status}
      `);
    },
    events: async function (fetchInfo, successCallback, failureCallback) {
      try {
        const year = fetchInfo.start.getFullYear();
        const month = fetchInfo.start.getMonth() + 1;

        await saveAutomaticSchedule(year, month);

        const response = await fetch(
          `${API_BASE_URL}/maintenance/all-machines/year/${year}`
        );
        if (!response.ok) throw new Error("Failed to fetch events");

        const data = await response.json();
        const apiEvents = data.map((event) => ({
          id: event.id,
          title: `${event.mesin.entityNo} - ${event.action}`,
          start: `${event.tahun}-${getMonthNumber(event.bulan)}-${event.tanggal
            .toString()
            .padStart(2, "0")}`,
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
          borderColor: event.isRescheduled ? "#c0392b" : "#27ae60",
          className: event.isRescheduled ? "reschedule-event" : "regular-event",
        }));

        successCallback(apiEvents);
      } catch (error) {
        console.error("Error fetching events:", error);
        failureCallback(error);
      }
    },
  });

  // ========================================================
  // Event Listeners
  // ========================================================
  elements.saveButton?.addEventListener("click", async function () {
    const success = await saveSchedule();
    if (success) {
      calendar.refetchEvents();
      closeModal();
    }
  });

  elements.printReportButton?.addEventListener("click", async function () {
    try {
      const currentDate = calendar.getDate();
      const year = currentDate.getFullYear();
      const month = currentDate.getMonth() + 1;

      const response = await fetch(
        `${API_BASE_URL}/maintenance/report/monthly?year=${year}&month=${month}`
      );
      if (!response.ok) throw new Error("Gagal mengambil laporan");

      const data = await response.json();
      const printContent = `
        <h1>Laporan Pemeliharaan Bulanan</h1>
        <h2>Bulan: ${getMonthName(month)} ${year}</h2>
        <table border="1" cellpadding="5" cellspacing="0" style="width:100%; border-collapse: collapse;">
          <thead>
            <tr>
              <th>No</th><th>Tanggal</th><th>Nomor Entitas</th>
              <th>Nama Entitas</th><th>Jenis Pemeliharaan</th>
              <th>Teknisi</th><th>Status</th><th>Deskripsi</th>
            </tr>
          </thead>
          <tbody>
            ${data
              .map(
                (item, index) => `
              <tr>
                <td>${index + 1}</td>
                <td>${item.tanggal}</td>
                <td>${item.mesin.entityNo}</td>
                <td>${item.mesin.entityName}</td>
                <td>${item.action}</td>
                <td>${item.technician}</td>
                <td>${item.status}</td>
                <td>${item.description || "-"}</td>
              </tr>
            `
              )
              .join("")}
          </tbody>
        </table>
      `;

      const printWindow = window.open("", "_blank");
      printWindow.document.write(`
        <html>
          <head>
            <title>Laporan Pemeliharaan Bulanan</title>
            <style>
              body { font-family: Arial, sans-serif; }
              table { width: 100%; border-collapse: collapse; }
              th, td { border: 1px solid #000; padding: 8px; text-align: left; }
              th { background-color: #f2f2f2; }
            </style>
          </head>
          <body>${printContent}</body>
        </html>
      `);
      printWindow.document.close();
      printWindow.print();
    } catch (error) {
      console.error("Error generating report:", error);
      alert("Gagal mencetak laporan. Silakan coba lagi.");
    }
  });

  elements.rescheduleEntityNumber?.addEventListener("change", (e) =>
    handleEntityNumberChange(e, "reschedule-entity-name", "reschedule-mesin-id")
  );

  elements.closeBtn?.addEventListener("click", closeModal);

  // ========================================================
  // Initialization
  // ========================================================
  calendar.render();
  populateMesinDropdown();
});
