// doctorServices.js
import { API_BASE_URL } from "../config/config.js";

const DOCTOR_API = API_BASE_URL + '/doctor';

export async function getDoctors() {
  try {
    const response = await fetch(DOCTOR_API);
    const data = await response.json();
    return data.doctors;
  } catch (error) {
    console.error("Error :: getDoctors :: ", error);
    return [];
  }
}

export async function deleteDoctor(id, token) {
  try {
    const response = await fetch(`${DOCTOR_API}/${id}/${token}`, {
      method: "DELETE"
    });

    const data = await response.json();
    return {
      success: response.ok,
      message: data.message
    };
  } catch (error) {
    console.error("Error :: deleteDoctor :: ", error);
    return { success: false, message: "Failed to delete doctor." };
  }
}

export async function saveDoctor(doctor, token) {
  try {
    const response = await fetch(`${DOCTOR_API}/${token}`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(doctor)
    });

    const data = await response.json();
    return {
      success: response.ok,
      message: data.message
    };
  } catch (error) {
    console.error("Error :: saveDoctor :: ", error);
    return { success: false, message: "Failed to save doctor." };
  }
}

export async function filterDoctors(name, time, specialty) {
  try {
    const n = name || "null";
    const t = time || "null";
    const s = specialty || "null";

    const response = await fetch(`${DOCTOR_API}/filter/${n}/${t}/${s}`, {
      method: "GET"
    });

    if (response.ok) {
      const data = await response.json();
      return data;
    } else {
      console.error("Failed to fetch doctors:", response.statusText);
      return { doctors: [] };
    }
  } catch (error) {
    console.error("Error :: filterDoctors :: ", error);
    alert("Something went wrong while filtering doctors.");
    return { doctors: [] };
  }
}
