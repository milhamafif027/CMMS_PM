/**
 * @fileoverview Main application logic for machine management system
 * @version 1.0.0
 */

document.addEventListener("DOMContentLoaded", function () {
  // ========================================================
  // Constants and Configuration
  // ========================================================
  const API_BASE_URL = "http://localhost:8080/api";

  // ========================================================
  // Utility Functions
  // ========================================================
  /**
   * Sanitizes input string to prevent XSS attacks
   * @param {string} str - Input string to sanitize
   * @returns {string} Sanitized string
   */
  const sanitizeInput = (str) => {
    const div = document.createElement("div");
    div.textContent = str;
    return div.innerHTML;
  };

  // ========================================================
  // Modal Management Class
  // ========================================================
  /**
   * Manages modal dialogs with animations and event handling
   */
  class ModalManager {
    /**
     * @param {string} modalId - ID of the modal element
     * @param {string} overlayClass - Class of the overlay element
     */
    constructor(modalId, overlayClass) {
      this.modal = document.getElementById(modalId);
      this.overlay = document.querySelector(overlayClass);
      this.isOpen = false;
      this.closeTimeout = null;
      this.setupEventListeners();
    }

    /**
     * Opens the modal with animation
     */
    open() {
      if (!this.modal || !this.overlay || this.isOpen) return;

      if (this.closeTimeout) {
        clearTimeout(this.closeTimeout);
        this.closeTimeout = null;
      }

      document.body.classList.add("modal-open");
      this.overlay.style.display = "block";
      this.modal.style.display = "block";

      this.modal.offsetHeight; // Force reflow

      requestAnimationFrame(() => {
        this.modal.classList.add("modal-active");
        this.isOpen = true;
      });
    }

    /**
     * Closes the modal with animation
     */
    close() {
      if (!this.modal || !this.overlay || !this.isOpen) return;

      this.modal.classList.remove("modal-active");
      this.isOpen = false;

      this.closeTimeout = setTimeout(() => {
        this.modal.style.display = "none";
        this.overlay.style.display = "none";
        document.body.classList.remove("modal-open");
        this.closeTimeout = null;
      }, 300);
    }

    /**
     * Sets up event listeners for modal interactions
     * @private
     */
    setupEventListeners() {
      if (!this.modal || !this.overlay) return;

      const handleClose = (e) => {
        if (e.target === this.overlay) {
          this.close();
        }
      };

      this.modal.addEventListener("click", (e) => e.stopPropagation());
      this.overlay.addEventListener("click", handleClose);

      const closeBtn = this.modal.querySelector(".close-btn");
      if (closeBtn) {
        closeBtn.addEventListener("click", (e) => {
          e.stopPropagation();
          this.close();
        });
      }

      document.addEventListener("keydown", (e) => {
        if (e.key === "Escape" && this.isOpen) {
          this.close();
        }
      });
    }
  }

  // ========================================================
  // API Integration
  // ========================================================
  /**
   * Fetches machine data from the API
   * @returns {Promise<Array>} Array of machine objects
   * @throws {Error} When API request fails
   */
  async function fetchMesin() {
    try {
      const response = await fetch(`${API_BASE_URL}/mesin`);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return await response.json();
    } catch (error) {
      console.error("Error fetching mesin:", error);
      throw new Error("Gagal mengambil data mesin. Silakan coba lagi.");
    }
  }

  /**
   * Creates a new machine entry via API
   * @param {Object} mesinData - Machine data object
   * @returns {Promise<Object>} Created machine object
   * @throws {Error} When API request fails
   */
  async function createMesin(mesinData) {
    try {
      const response = await fetch(`${API_BASE_URL}/mesin`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(mesinData),
      });

      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message || "Gagal membuat mesin");
      }

      return await response.json();
    } catch (error) {
      console.error("Error creating mesin:", error);
      throw error;
    }
  }

  // ========================================================
  // Table Management
  // ========================================================
  /**
   * Loads and displays machine data in the table
   */
  async function loadTableData() {
    const tbody = document.querySelector("tbody");
    if (!tbody) return;

    try {
      const mesinList = await fetchMesin();
      tbody.innerHTML = "";

      mesinList.forEach((mesin, index) => {
        const row = document.createElement("tr");

        const cells = [
          { content: (index + 1).toString() },
          { content: sanitizeInput(mesin.entityNo) },
          { content: sanitizeInput(mesin.entityName) },
          { content: sanitizeInput(mesin.brandType) },
          { content: mesin.qtyGrm.toString() },
          {
            content: `
              <button class="btn btn-view" data-mesin-id="${sanitizeInput(
                mesin.id
              )}" aria-label="Lihat Detail Mesin">
                <i class="fas fa-eye"></i>
              </button>
            `,
            isHTML: true,
          },
        ];

        cells.forEach(({ content, isHTML }) => {
          const td = document.createElement("td");
          if (isHTML) {
            td.innerHTML = content;
          } else {
            td.textContent = content;
          }
          row.appendChild(td);
        });

        tbody.appendChild(row);
      });
    } catch (error) {
      console.error("Error loading table data:", error);
      alert(error.message || "Gagal memuat data mesin");
    }
  }

  // ========================================================
  // Form Validation
  // ========================================================
  /**
   * Validates machine form data
   * @param {Object} formData - Form data object to validate
   * @returns {Array} Array of error messages, empty if valid
   */
  function validateMesinData(formData) {
    const errors = [];

    if (!formData.entityNo?.trim()) {
      errors.push("Nomor entitas harus diisi");
    }

    if (!formData.entityName?.trim()) {
      errors.push("Nama entitas harus diisi");
    }

    if (!formData.brandType?.trim()) {
      errors.push("Tipe merek harus diisi");
    }

    if (isNaN(formData.qtyGrm) || formData.qtyGrm < 0) {
      errors.push("Kuantitas harus berupa angka positif");
    }

    return errors;
  }

  // ========================================================
  // Form Handling
  // ========================================================
  const addMachineForm = document.getElementById("add-machine-form");
  if (addMachineForm) {
    addMachineForm.addEventListener("submit", async (e) => {
      e.preventDefault();

      const formData = {
        entityNo: document.getElementById("new-entity-no").value.trim(),
        entityName: document.getElementById("new-entity-name").value.trim(),
        brandType: document.getElementById("new-brand").value.trim(),
        qtyGrm: parseInt(document.getElementById("new-qty").value),
      };

      const errors = validateMesinData(formData);
      if (errors.length > 0) {
        alert(errors.join("\n"));
        return;
      }

      try {
        await createMesin(formData);
        await loadTableData();
        addMachineForm.reset();
        addModal.close();
      } catch (error) {
        alert(error.message || "Gagal menambahkan mesin!");
      }
    });
  }

  // ========================================================
  // View Detail Functionality
  // ========================================================
  document.querySelector("tbody")?.addEventListener("click", async (e) => {
    const viewButton = e.target.closest(".btn-view");
    if (!viewButton) return;

    const row = viewButton.closest("tr");
    if (!row) return;

    const cells = row.cells;
    const modalFields = {
      "modal-id": cells[0]?.textContent,
      "modal-entity-no": cells[1]?.textContent,
      "modal-entity-name": cells[2]?.textContent,
      "modal-brand": cells[3]?.textContent,
      "modal-qty": cells[4]?.textContent,
    };

    // Update modal content
    Object.entries(modalFields).forEach(([id, value]) => {
      const element = document.getElementById(id);
      if (element) {
        element.textContent = value || "N/A";
      }
    });

    // Buka modal detail
    detailModal.open();
  });

  // ========================================================
  // Filter Implementation
  // ========================================================
  /**
   * Sets up table filtering functionality
   */
  const setupFilter = () => {
    const entityNoFilter = document.getElementById("filter-entityNo");
    const entityNameFilter = document.getElementById("filter-entityName");
    let debounceTimer;

    const filterTable = () => {
      const noFilter = entityNoFilter?.value.toLowerCase().trim() || "";
      const nameFilter = entityNameFilter?.value.toLowerCase().trim() || "";

      document.querySelectorAll("tbody tr").forEach((row) => {
        const entityNo = (row.cells[1]?.textContent || "").toLowerCase();
        const entityName = (row.cells[2]?.textContent || "").toLowerCase();

        const matchesFilter =
          entityNo.includes(noFilter) && entityName.includes(nameFilter);

        row.style.display = matchesFilter ? "" : "none";
      });
    };

    const debouncedFilter = () => {
      clearTimeout(debounceTimer);
      debounceTimer = setTimeout(filterTable, 300);
    };

    entityNoFilter?.addEventListener("input", debouncedFilter);
    entityNameFilter?.addEventListener("input", debouncedFilter);
  };

  // ==============================================
  // SWIPER SLIDER INITIALIZATION
  // ==============================================
  const swiperContainer = document.querySelector(".swiper");
  if (swiperContainer) {
    new Swiper(".swiper", {
      loop: true,
      autoplay: {
        delay: 3000,
        disableOnInteraction: false,
      },
      pagination: {
        el: ".swiper-pagination",
        clickable: true,
      },
      navigation: {
        nextEl: ".swiper-button-next",
        prevEl: ".swiper-button-prev",
      },
      effect: "fade",
      fadeEffect: {
        crossFade: true,
      },
      observer: true,
      observeParents: true,
    });
  }

  // ========================================================
  // Application Initialization
  // ========================================================

  // Initialize modals
  const detailModal = new ModalManager("detail-modal", "#detail-modal-overlay");
  const addModal = new ModalManager("add-machine-modal", "#add-modal-overlay");

  const addMachineBtn = document.getElementById("open-add-modal");
  if (addMachineBtn) {
    addMachineBtn.addEventListener("click", () => addModal.open());
  }

  // Start the application
  loadTableData();
  setupFilter();
});
