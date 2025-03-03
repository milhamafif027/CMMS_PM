document.addEventListener("DOMContentLoaded", function () {
  const API_BASE_URL = "http://localhost:8080/api";

  const elements = {
    calendar: document.getElementById("calendar"),
    modal: document.getElementById("event-modal"),
    overlay: document.querySelector(".modal-overlay"),
    saveButton: document.getElementById("save-event"),
    closeBtn: document.getElementById("close-modal"),
    scheduleForm: document.getElementById("schedule-form"),
    rescheduleForm: document.getElementById("reschedule-form"),
    scheduleTypeRadios: document.querySelectorAll(
      'input[name="schedule-type"]'
    ),
    entityNumber: document.getElementById("entity-number"),
    entityName: document.getElementById("entity-name"),
    mesinId: document.getElementById("mesin-id"),
    rescheduleEntityNumber: document.getElementById("reschedule-entity-number"),
    rescheduleEntityName: document.getElementById("reschedule-entity-name"),
    rescheduleId: document.getElementById("reschedule-mesin-id"),
    printReportButton: document.getElementById("print-report"),
  };

  let selectedDate = null;

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

  function getMonthNumber(monthName) {
    return months[monthName] || "01";
  }

  function getMonthName(monthNumber) {
    return monthNames[monthNumber - 1] || "Januari";
  }

  function parseDateParts(dateStr) {
    const dateParts = dateStr.split("-");
    return {
      year: parseInt(dateParts[0]),
      month: parseInt(dateParts[1]),
      day: parseInt(dateParts[2]),
      monthName: getMonthName(parseInt(dateParts[1])),
    };
  }

  function isTodayDate(date) {
    const today = new Date();
    return (
      date.getDate() === today.getDate() &&
      date.getMonth() === today.getMonth() &&
      date.getFullYear() === today.getFullYear()
    );
  }

  function openModal() {
    const selectedDateObj = new Date(selectedDate);
    if (isTodayDate(selectedDateObj)) {
      elements.modal.style.display = "block";
      elements.overlay.style.display = "block";
    } else {
      alert("Anda hanya dapat menjadwalkan maintenance untuk hari ini.");
    }
  }

  function closeModal() {
    elements.modal.style.display = "none";
    elements.overlay.style.display = "none";
    resetForm();
  }

  function resetForm() {
    const formElements = [
      "entity-number",
      "entity-name",
      "maintenance-type",
      "description",
      "technician",
      "reschedule-entity-number",
      "reschedule-entity-name",
      "reschedule-reason",
      "reschedule-maintenance-type",
      "reschedule-technician",
    ];

    formElements.forEach((id) => {
      const element = document.getElementById(id);
      if (element) element.value = "";
    });
  }

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

  async function populateMesinDropdown() {
    try {
      const response = await fetch(`${API_BASE_URL}/mesin`);
      if (!response.ok) throw new Error("Gagal mengambil data mesin");
      const mesinList = await response.json();

      const dropdowns = [
        elements.entityNumber,
        elements.rescheduleEntityNumber,
      ];

      dropdowns.forEach((dropdown) => {
        if (!dropdown) {
          console.error("Dropdown element not found");
          return;
        }
        dropdown.innerHTML = "";
        const defaultOption = document.createElement("option");
        defaultOption.value = "";
        defaultOption.text = "Pilih Nomor Entitas";
        dropdown.appendChild(defaultOption);

        mesinList.forEach((mesin) => {
          const option = document.createElement("option");
          option.value = mesin.entityNo;
          option.text = `${mesin.entityNo} - ${mesin.entityName}`;
          dropdown.appendChild(option);
        });
      });
    } catch (error) {
      console.error("Error fetching mesin list:", error);
    }
  }

  async function saveSchedule(isReschedule) {
    try {
      const dateInfo = parseDateParts(selectedDate);
      const commonData = {
        bulan: dateInfo.monthName,
        tanggal: dateInfo.day,
        tahun: dateInfo.year,
        isRescheduled: isReschedule,
      };

      let specificData;

      if (!isReschedule) {
        const mesinId = elements.mesinId?.value;
        const maintenanceType =
          document.getElementById("maintenance-type")?.value;
        const description = document.getElementById("description")?.value;
        const technician = document.getElementById("technician")?.value;

        if (!elements.entityNumber?.value || !maintenanceType || !technician) {
          throw new Error("Semua field harus diisi!");
        }

        specificData = {
          mesin: { id: mesinId },
          status: "Scheduled",
          action: maintenanceType,
          description: description || "-",
          technician: technician,
          rescheduleReason: null,
        };
      } else {
        const mesinId = elements.rescheduleId?.value;
        const reason = document.getElementById("reschedule-reason")?.value;
        const maintenanceType = document.getElementById(
          "reschedule-maintenance-type"
        )?.value;
        const technician = document.getElementById(
          "reschedule-technician"
        )?.value;

        if (
          !elements.rescheduleEntityNumber?.value ||
          !reason ||
          !maintenanceType ||
          !technician
        ) {
          throw new Error("Semua field harus diisi!");
        }

        specificData = {
          mesin: { id: mesinId },
          status: "Rescheduled",
          action: maintenanceType,
          description: reason,
          technician: technician,
          rescheduleReason: reason,
        };
      }

      const requestData = { ...commonData, ...specificData };
      const response = await fetch(`${API_BASE_URL}/maintenance`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(requestData),
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(
          `Gagal menyimpan ${isReschedule ? "reschedule" : "jadwal"}`
        );
      }

      alert("Jadwal berhasil disimpan!");
      return true;
    } catch (error) {
      console.error("Save error:", error);
      alert(error.message || "Terjadi kesalahan saat menyimpan data.");
      return false;
    }
  }

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
      if (isTodayDate(info.date)) {
        selectedDate = info.dateStr;
        openModal();
      } else {
        alert("Anda hanya dapat menjadwalkan maintenance untuk hari ini.");
      }
    },
    dayCellClassNames: function (arg) {
      return !isTodayDate(arg.date) ? ["fc-day-disabled"] : [];
    },
    eventClick: function (info) {
      const event = info.event;
      const props = event.extendedProps;
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
        const response = await fetch(
          `${API_BASE_URL}/maintenance/all-machines/year/${year}`
        );
        if (!response.ok) {
          const errorText = await response.text();
          throw new Error("Failed to fetch events");
        }

        const data = await response.json();
        if (!Array.isArray(data)) {
          throw new Error("Invalid data format");
        }

        const events = data.map((event) => {
          const monthNum = getMonthNumber(event.bulan);
          const day = event.tanggal.toString().padStart(2, "0");
          const dateStr = `${event.tahun}-${monthNum}-${day}`;

          return {
            id: event.id,
            title: `${event.mesin.entityNo} - ${event.action}`,
            start: dateStr,
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
            className: event.isRescheduled
              ? "reschedule-event"
              : "regular-event",
          };
        });

        successCallback(events);
      } catch (error) {
        console.error("Error fetching events:", error);
        failureCallback(error);
      }
    },
  });

  async function handleEntityNumberChange(
    e,
    targetNameField,
    targetIdField,
    isReschedule = false
  ) {
    const entityNo = e.target.value;
    const targetNameElement = document.getElementById(targetNameField);
    const targetIdElement = document.getElementById(targetIdField);

    if (!targetNameElement || !targetIdElement) {
      console.error(
        `Error: Element with ID ${targetNameField} or ${targetIdField} not found.`
      );
      return;
    }

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
        console.error("Error in handleEntityNumberChange:", error);
        targetNameElement.value = "";
        targetIdElement.value = "";
        if (!isReschedule) {
          alert("Mesin tidak ditemukan. Pastikan nomor entitas benar.");
        }
      }
    } else {
      targetNameElement.value = "";
      targetIdElement.value = "";
    }
  }

  async function handleSaveEvent() {
    const isReschedule =
      document.querySelector('input[name="schedule-type"]:checked').value ===
      "reschedule";
    const success = await saveSchedule(isReschedule);

    if (success) {
      calendar.refetchEvents();
      closeModal();
    }
  }

  function handleScheduleTypeChange(e) {
    const isReschedule = e.target.value === "reschedule";
    elements.scheduleForm.style.display = isReschedule ? "none" : "block";
    elements.rescheduleForm.style.display = isReschedule ? "block" : "none";
  }

  elements.printReportButton.addEventListener("click", async function () {
    try {
      const currentDate = calendar.getDate();
      const year = currentDate.getFullYear();
      const month = currentDate.getMonth() + 1;

      const response = await fetch(
        `${API_BASE_URL}/maintenance/report/monthly?year=${year}&month=${month}`
      );

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error("Gagal mengambil laporan bulanan");
      }

      const data = await response.json();

      const printContent = `
        <h1>Laporan Pemeliharaan Bulanan</h1>
        <h2>Bulan: ${getMonthName(month)} ${year}</h2>
        <table border="1" cellpadding="5" cellspacing="0" style="width:100%; border-collapse: collapse;">
          <thead>
            <tr>
              <th>No</th>
              <th>Tanggal</th>
              <th>Nomor Entitas</th>
              <th>Nama Entitas</th>
              <th>Jenis Pemeliharaan</th>
              <th>Teknisi</th>
              <th>Status</th>
              <th>Deskripsi</th>
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
          <body>
            ${printContent}
          </body>
        </html>
      `);
      printWindow.document.close();
      printWindow.print();
    } catch (error) {
      console.error("Error generating report:", error);
      alert("Gagal mencetak laporan. Silakan coba lagi.");
    }
  });

  elements.sche;
  elements.scheduleTypeRadios.forEach((radio) => {
    radio.addEventListener("change", handleScheduleTypeChange);
  });

  elements.entityNumber.addEventListener("change", (e) =>
    handleEntityNumberChange(e, "entity-name", "mesin-id")
  );

  elements.rescheduleEntityNumber.addEventListener("change", (e) =>
    handleEntityNumberChange(
      e,
      "reschedule-entity-name",
      "reschedule-mesin-id",
      true
    )
  );

  elements.saveButton.addEventListener("click", handleSaveEvent);
  elements.closeBtn.addEventListener("click", closeModal);

  calendar.render();
  populateMesinDropdown();
});
