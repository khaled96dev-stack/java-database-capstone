// index.js
import { openModal } from "../components/modals.js";
import { API_BASE_URL } from "../config/config.js";

const ADMIN_API = API_BASE_URL + '/admin';
const DOCTOR_API = API_BASE_URL + '/doctor/login';

window.onload = function () {
  const adminBtn = document.getElementById('adminLogin');
  if (adminBtn) {
    adminBtn.addEventListener('click', () => {
      openModal('adminLogin');
    });
  }

  const doctorBtn = document.getElementById('doctorLogin');
  if (doctorBtn) {
    doctorBtn.addEventListener('click', () => {
      openModal('doctorLogin');
    });
  }
};

window.adminLoginHandler = async function () {
  try {
    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;

    const admin = { username, password };

    const response = await fetch(ADMIN_API, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(admin)
    });

    if (response.ok) {
      const result = await response.json();
      localStorage.setItem('token', result.token);
      selectRole('admin');
    } else {
      alert('Invalid credentials!');
    }
  } catch (error) {
    console.error("Error :: adminLoginHandler :: ", error);
    alert("Something went wrong. Please try again later.");
  }
};

window.doctorLoginHandler = async function () {
  try {
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    const doctor = { email, password };

    const response = await fetch(DOCTOR_API, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(doctor)
    });

    if (response.ok) {
      const result = await response.json();
      localStorage.setItem('token', result.token);
      selectRole('doctor');
    } else {
      alert('Invalid credentials!');
    }
  } catch (error) {
    console.error("Error :: doctorLoginHandler :: ", error);
    alert("Something went wrong. Please try again later.");
  }
};
