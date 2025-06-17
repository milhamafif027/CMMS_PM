document.addEventListener("DOMContentLoaded", function () {
  // ===================== CONFIGURATION =====================
  const API_BASE_URL = "http://localhost:8080/api/maintenance";
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

  // ===================== DOM ELEMENTS =====================
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
    originalDateDisplay: document.getElementById("reschedule-original-date"),
    reasonInput: document.getElementById("reschedule-reason"),
    maintenanceTypeInput: document.getElementById(
      "reschedule-maintenance-type"
    ),
    technicianInput: document.getElementById("reschedule-technician"),
    reportYear: document.getElementById("report-year"),
    reportMonth: document.getElementById("report-month"),
    errorDisplay:
      document.getElementById("error-message") || createErrorElement(),
  };

  function createErrorElement() {
    const div = document.createElement("div");
    div.id = "error-message";
    div.style.cssText = `
      position: fixed;
      top: 20px;
      right: 20px;
      padding: 15px;
      background: #ff4444;
      color: white;
      border-radius: 5px;
      z-index: 1000;
      display: none;
    `;
    document.body.appendChild(div);
    return div;
  }

  // ===================== STATE VARIABLES =====================
  let selectedDate = null;
  let currentOriginalSchedule = null;
  let calendar = null;

  // ===================== UTILITY FUNCTIONS =====================
  function showNotification(type, message) {
    // Perbaikan: Gunakan console.log untuk semua notifikasi
    console.log(`[${type.toUpperCase()}] ${message}`);

    if (elements.errorDisplay) {
      elements.errorDisplay.textContent = message;
      elements.errorDisplay.style.background =
        type === "error" ? "#ff4444" : "#4CAF50";
      elements.errorDisplay.style.display = "block";

      setTimeout(
        () => {
          elements.errorDisplay.style.display = "none";
          if (type === "success") {
            elements.errorDisplay.style.background = "#ff4444";
          }
        },
        type === "error" ? 5000 : 3000
      );
    }
  }

  function getMonthNumber(monthName) {
    return months[monthName] || "01";
  }

  function parseDateParts(dateStr) {
    if (!dateStr) {
      showNotification("error", "Invalid date string");
      return { year: 0, month: 1, day: 1, monthName: "Januari" };
    }

    try {
      const date = new Date(dateStr);
      if (isNaN(date.getTime())) {
        throw new Error("Invalid date");
      }
      return {
        year: date.getFullYear(),
        month: date.getMonth() + 1,
        day: date.getDate(),
        monthName: Object.keys(months)[date.getMonth()],
      };
    } catch (error) {
      console.error("Error parsing date:", error);
      showNotification("error", "Format tanggal tidak valid");
      return { year: 0, month: 1, day: 1, monthName: "Januari" };
    }
  }

  function determineEndOfWeekDate(date) {
    if (!date || isNaN(date)) return 28;
    if (date <= 7) return 7;
    if (date <= 14) return 14;
    if (date <= 21) return 21;
    return 28;
  }

  function isChangingWeek(originalDate, newDate) {
    return (
      determineEndOfWeekDate(originalDate) !== determineEndOfWeekDate(newDate)
    );
  }

  // ===================== MODAL MANAGEMENT =====================
  function openModal(dateStr) {
    if (!elements.modal || !elements.overlay) {
      showNotification("error", "Modal elements not found");
      return;
    }

    selectedDate = dateStr;
    resetForm();

    elements.rescheduleForm.addEventListener("click", function (e) {
      e.stopPropagation();
    });

    elements.modal.style.display = "block";
    elements.overlay.style.display = "block";

    setTimeout(() => {
      if (elements.rescheduleEntityNumber) {
        elements.rescheduleEntityNumber.focus();
      }
    }, 100);
  }

  function closeModal() {
    if (elements.rescheduleForm) {
      elements.rescheduleForm.removeEventListener("click", function (e) {
        e.stopPropagation();
      });
    }

    if (elements.modal) elements.modal.style.display = "none";
    if (elements.overlay) elements.overlay.style.display = "none";
    resetForm();
    currentOriginalSchedule = null;
  }

  function resetForm() {
    if (elements.rescheduleEntityNumber)
      elements.rescheduleEntityNumber.value = "";
    if (elements.rescheduleEntityName) elements.rescheduleEntityName.value = "";
    if (elements.reasonInput) elements.reasonInput.value = "";
    if (elements.maintenanceTypeInput) elements.maintenanceTypeInput.value = "";
    if (elements.technicianInput) elements.technicianInput.value = "";
    if (elements.originalDateDisplay) {
      elements.originalDateDisplay.textContent = "Pilih mesin terlebih dahulu";
    }
  }

  // ===================== API FUNCTIONS =====================
  async function fetchWithErrorHandling(url, options = {}) {
    try {
      const response = await fetch(url, {
        ...options,
        headers: {
          "Content-Type": "application/json",
          ...(options.headers || {}),
        },
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(
          errorData.message || `HTTP error! status: ${response.status}`
        );
      }
      return await response.json();
    } catch (error) {
      console.error(`Fetch error for ${url}:`, error);
      showNotification("error", error.message || "Gagal memproses permintaan");
      throw error;
    }
  }

  async function fetchMesinByNumber(entityNo) {
    if (!entityNo) {
      showNotification("error", "Nomor entitas harus diisi");
      return null;
    }
    return fetchWithErrorHandling(
      `${API_BASE_URL}/mesin/by-number/${entityNo}`
    );
  }

  async function populateMesinDropdown() {
    try {
      const mesinList = await fetchWithErrorHandling(`${API_BASE_URL}/mesin`);
      if (!elements.rescheduleEntityNumber) return;

      elements.rescheduleEntityNumber.innerHTML =
        '<option value="">Pilih Nomor Entitas</option>';
      mesinList.forEach((mesin) => {
        const option = document.createElement("option");
        option.value = mesin.entityNo;
        option.textContent = `${mesin.entityNo} - ${mesin.entityName}`;
        elements.rescheduleEntityNumber.appendChild(option);
      });
    } catch (error) {
      console.error("Error fetching mesin list:", error);
      showNotification("error", "Gagal memuat daftar mesin");
    }
  }

  async function findOriginalAutoSchedule(dateInfo, mesinId) {
    if (!mesinId) {
      showNotification("error", "ID mesin tidak valid");
      return null;
    }

    try {
      const schedules = await fetchWithErrorHandling(
        `${API_BASE_URL}/by-mesin-month?mesinId=${mesinId}&year=${dateInfo.year}&month=${dateInfo.month}`
      );

      const endOfWeekDate = determineEndOfWeekDate(dateInfo.day);
      return schedules.find(
        (s) =>
          s.tanggal === endOfWeekDate &&
          !s.isRescheduled &&
          s.action === "Pemeliharaan Rutin"
      );
    } catch (error) {
      console.error("Error finding original schedule:", error);
      return null;
    }
  }

  // ===================== SCHEDULE FUNCTIONS =====================
  async function saveSchedule() {
    if (!elements.saveButton) return false;

    const originalButtonText = elements.saveButton.textContent;
    try {
      elements.saveButton.disabled = true;
      elements.saveButton.textContent = "Menyimpan...";

      const dateInfo = parseDateParts(selectedDate);
      if (!dateInfo.year || !dateInfo.month || !dateInfo.day) {
        throw new Error("Tanggal tidak valid");
      }

      // Validasi field wajib
      const requiredFields = {
        "Nomor Entitas": elements.rescheduleEntityNumber?.value,
        Alasan: elements.reasonInput?.value,
        "Jenis Maintenance": elements.maintenanceTypeInput?.value,
        Teknisi: elements.technicianInput?.value,
      };

      const missingFields = Object.entries(requiredFields)
        .filter(([_, value]) => !value)
        .map(([name]) => name);

      if (missingFields.length > 0) {
        throw new Error(`Field wajib: ${missingFields.join(", ")}`);
      }

      const mesinId = elements.rescheduleId?.value;
      if (!mesinId) {
        throw new Error("ID mesin tidak valid");
      }

      if (!currentOriginalSchedule) {
        currentOriginalSchedule = await findOriginalAutoSchedule(
          dateInfo,
          mesinId
        );
        if (!currentOriginalSchedule) {
          throw new Error("Jadwal asli tidak ditemukan");
        }
      }

      // Payload untuk reschedule
      const payload = {
        newDate: dateInfo.day,
        reason: elements.reasonInput.value,
        maintenanceType: elements.maintenanceTypeInput.value,
        technician: elements.technicianInput.value,
        tahun: dateInfo.year,
      };

      console.log("Payload yang dikirim:", payload);

      // Kirim request reschedule
      const response = await fetchWithErrorHandling(
        `${API_BASE_URL}/reschedule/${currentOriginalSchedule.id}`,
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(payload),
        }
      );

      // Jika pindah minggu, hapus jadwal otomatis
      if (isChangingWeek(currentOriginalSchedule.tanggal, dateInfo.day)) {
        const endOfWeekDate = determineEndOfWeekDate(
          currentOriginalSchedule.tanggal
        );
        const startDate = endOfWeekDate - 6;

        await fetchWithErrorHandling(`${API_BASE_URL}/cleanup-weekly`, {
          method: "DELETE",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            year: dateInfo.year,
            month: getMonthNumber(dateInfo.monthName),
            startDate: startDate,
            endDate: endOfWeekDate,
            mesinId: mesinId,
          }),
        });
      }

      showNotification("success", "Reschedule berhasil disimpan!");
      calendar.refetchEvents();
      closeModal();
      return true;
    } catch (error) {
      console.error("Save error:", error);
      showNotification("error", error.message || "Gagal menyimpan perubahan");
      return false;
    } finally {
      elements.saveButton.disabled = false;
      elements.saveButton.textContent = originalButtonText;
    }
  }
  // ===================== REPORT FUNCTIONS =====================
  async function generateReport() {
    try {
      const currentDate = calendar.getDate(); // ambil tanggal yang sedang ditampilkan
      const year = currentDate.getFullYear();
      const monthIndex = currentDate.getMonth(); // 0–11
      const monthNames = Object.keys(months);
      const monthName = monthNames[monthIndex];
      const monthNumber = months[monthName];

      if (!year || !monthNumber) {
        throw new Error("Bulan dan tahun tidak valid");
      }

      elements.printReportButton.disabled = true;
      elements.printReportButton.textContent = "Membuat laporan...";

      const response = await fetch(
        `${API_BASE_URL}/report/monthly/pdf?year=${year}&month=${monthNumber}`,
        {
          headers: {
            Accept: "application/pdf",
          },
        }
      );

      if (!response.ok) {
        throw new Error("Gagal menghasilkan laporan");
      }

      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `Laporan_Pemeliharaan_${monthName}_${year}.pdf`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      window.URL.revokeObjectURL(url);

      showNotification("success", "Laporan berhasil diunduh");
    } catch (error) {
      console.error("Report error:", error);
      showNotification("error", error.message);
    } finally {
      elements.printReportButton.disabled = false;
      elements.printReportButton.textContent = "Cetak Laporan Bulanan";
    }
  }
  

  // ===================== CALENDAR INITIALIZATION =====================
  
  function initializeCalendar() {
    if (!elements.calendar) {
      showNotification("error", "Calendar element not found");
      return;
    }

    calendar = new FullCalendar.Calendar(elements.calendar, {
      initialView: "dayGridMonth",
      headerToolbar: {
        left: "prev,next today",
        center: "title",
        right: "dayGridMonth,timeGridWeek,timeGridDay",
      },
      locale: "id",
      dayMaxEvents: true,
      eventDisplay: "block",
      eventTextColor: "#FFFFFF",
      selectable: true,
      dateClick: function (info) {
        openModal(info.dateStr);
      },
      eventClick: function (info) {
        const props = info.event.extendedProps;
        alert(`
          Entitas: ${props.entityName || "-"}
          Jenis: ${props.action || "-"}
          Teknisi: ${props.technician || "-"}
          Deskripsi: ${props.description || "-"}
          Status: ${props.status || "-"}
          ${
            props.isRescheduled
              ? `Alasan Reschedule: ${props.rescheduleReason || "-"}`
              : ""
          }
        `);
      },
      events: async function (fetchInfo, successCallback, failureCallback) {
        try {
          const year = fetchInfo.start.getFullYear();
          const data = await fetchWithErrorHandling(
            `${API_BASE_URL}/all-machines/year/${year}`
          );

          const apiEvents = data.map((event) => {
            const isRescheduled = event.isRescheduled;
            return {
              id: event.id,
              title: `${event.mesin?.entityNo || "Unknown"} - ${
                event.action || "Maintenance"
              }`,
              start: `${event.tahun}-${getMonthNumber(
                event.bulan
              )}-${event.tanggal.toString().padStart(2, "0")}`,
              allDay: true,
              extendedProps: {
                entityNo: event.mesin?.entityNo,
                entityName: event.mesin?.entityName,
                action: event.action,
                description: event.description,
                technician: event.technician,
                isRescheduled: isRescheduled,
                rescheduleReason: event.rescheduleReason,
                status: event.status,
              },
              // Color settings directly in JavaScript
              backgroundColor: isRescheduled ? "#FFD700" : "#4CAF50",
              borderColor: isRescheduled ? "#FFC600" : "#2E7D32",
              textColor: "#ffffff",
              display: "block",
              borderWidth: 2,
              fontWeight: "bold",
            };
          });

          successCallback(apiEvents);
        } catch (error) {
          console.error("Error loading events:", error);
          failureCallback(error);
        }
      },
      // Additional visual customization
      eventDidMount: function (info) {
        // Add subtle shadow for better visibility
        info.el.style.boxShadow = "0 2px 5px rgba(0,0,0,0.2)";
        info.el.style.borderRadius = "6px";
        info.el.style.padding = "2px 4px";
        info.el.style.fontWeight = "600";

        // Add hover effect
        info.el.addEventListener("mouseenter", function () {
          this.style.opacity = "0.9";
          this.style.transform = "scale(1.02)";
        });
        info.el.addEventListener("mouseleave", function () {
          this.style.opacity = "1";
          this.style.transform = "scale(1)";
        });
      },
    });

    calendar.render();
  }
  // ===================== EVENT LISTENERS =====================
  function initializeEventListeners() {
    if (elements.saveButton) {
      elements.saveButton.addEventListener("click", async function (e) {
        e.preventDefault();
        e.stopPropagation();
        await saveSchedule();
      });
    }

    if (elements.printReportButton) {
      elements.printReportButton.addEventListener("click", async function (e) {
        e.preventDefault();
        e.stopPropagation();
        await generateReport();
      });
    }
    
    if (elements.closeBtn) {
      elements.closeBtn.addEventListener("click", function (e) {
        e.preventDefault();
        e.stopPropagation();
        closeModal();
      });
    }

    if (elements.overlay) {
      elements.overlay.addEventListener("click", closeModal);
    }

    if (elements.rescheduleEntityNumber) {
      elements.rescheduleEntityNumber.addEventListener(
        "change",
        async function (e) {
          e.stopPropagation();
          const entityNo = this.value;
          if (!entityNo) return;

          try {
            const mesin = await fetchMesinByNumber(entityNo);
            if (!mesin) {
              throw new Error("Mesin tidak ditemukan");
            }

            elements.rescheduleEntityName.value = mesin.entityName;
            elements.rescheduleId.value = mesin.id;

            if (selectedDate) {
              const dateInfo = parseDateParts(selectedDate);
              currentOriginalSchedule = await findOriginalAutoSchedule(
                dateInfo,
                mesin.id
              );
              if (elements.originalDateDisplay) {
                elements.originalDateDisplay.textContent =
                  currentOriginalSchedule
                    ? `Jadwal asli: ${currentOriginalSchedule.tanggal} ${currentOriginalSchedule.bulan} ${currentOriginalSchedule.tahun}`
                    : "Tidak ada jadwal otomatis ditemukan";
              }
            }
          } catch (error) {
            console.error("Error:", error);
            elements.rescheduleEntityName.value = "";
            elements.rescheduleId.value = "";
            if (elements.originalDateDisplay) {
              elements.originalDateDisplay.textContent =
                "Gagal memuat data mesin";
            }
          }
        }
      );

      elements.rescheduleEntityNumber.addEventListener(
        "mousedown",
        function (e) {
          e.stopPropagation();
        }
      );
    }
  }

  // ===================== INITIALIZATION =====================
  function initialize() {
    if (!elements.calendar) {
      showNotification(
        "error",
        "Calendar element not found - cannot initialize"
      );
      return;
    }

    initializeCalendar();
    populateMesinDropdown();
    initializeEventListeners();
  }

  // Start the application
  initialize();
});
