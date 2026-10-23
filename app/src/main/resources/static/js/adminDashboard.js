// adminDashboard.js
import { openModal } from "./components/modals.js";
import { getDoctors, filterDoctors, saveDoctor } from "./services/doctorServices.js";
import { createDoctorCard } from "./components/doctorCard.js";

document.addEventListener("DOMContentLoaded", () => {
  const addDocBtn = document.getElementById("addDocBtn");
  if (addDocBtn) {
    addDocBtn.addEventListener("click", () => {
      openModal("addDoctor");
    });
  }

  loadDoctorCards();

  const searchBar = document.getElementById("searchBar");
  const filterTime = document.getElementById("filterTime");
  const filterSpecialty = document.getElementById("filterSpecialty");

  if (searchBar) searchBar.addEventListener("input", filterDoctorsOnChange);
  if (filterTime) filterTime.addEventListener("change", filterDoctorsOnChange);
  if (filterSpecialty) filterSpecialty.addEventListener("change", filterDoctorsOnChange);
});

function loadDoctorCards() {
  getDoctors()
    .then(doctors => {
      renderDoctorCards(doctors);
    })
    .catch(error => {
      console.error("Failed to load doctors:", error);
    });
}

function filterDoctorsOnChange() {
  const searchBar = document.getElementById("searchBar").value.trim();
  const filterTime = document.getElementById("filterTime").value;
  const filterSpecialty = document.getElementById("filterSpecialty").value;

  const name = searchBar.length > 0 ? searchBar : null;
  const time = filterTime.length > 0 ? filterTime : null;
  const specialty = filterSpecialty.length > 0 ? filterSpecialty : null;

  filterDoctors(name, time, specialty)
    .then(response => {
      const doctors = response.doctors;
      const contentDiv = document.getElementById("content");
      contentDiv.innerHTML = "";

      if (doctors && doctors.length > 0) {
        renderDoctorCards(doctors);
      } else {
        contentDiv.innerHTML = "<p>No doctors found with the given filters.</p>";
      }
    })
    .catch(error => {
      console.error("Failed to filter doctors:", error);
      alert("❌ An error occurred while filtering doctors.");
    });
}

function renderDoctorCards(doctors) {
  const contentDiv = document.getElementById("content");
  contentDiv.innerHTML = "";

  doctors.forEach(doctor => {
    const card = createDoctorCard(doctor);
    contentDiv.appendChild(card);
  });
}

window.adminAddDoctor = async function () {
  try {
    const name = document.getElementById("doctorName").value;
    const specialty = document.getElementById("specialization").value;
    const email = document.getElementById("doctorEmail").value;
    const password = document.getElementById("doctorPassword").value;
    const phone = document.getElementById("doctorPhone").value;

    const checkboxes = document.querySelectorAll('input[name="availability"]:checked');
    const availableTimes = Array.from(checkboxes).map(cb => cb.value);

    const token = localStorage.getItem("token");
    if (!token) {
      alert("You must be logged in as admin to perform this action.");
      return;
    }

    const doctor = { name, specialty, email, password, phone, availableTimes };

    const { success, message } = await saveDoctor(doctor, token);

    if (success) {
      alert("Doctor added successfully.");
      document.getElementById("modal").style.display = "none";
      loadDoctorCards();
    } else {
      alert(message || "Failed to add doctor.");
    }
  } catch (error) {
    console.error("Error :: adminAddDoctor :: ", error);
    alert("❌ An error occurred while adding the doctor.");
  }
};
