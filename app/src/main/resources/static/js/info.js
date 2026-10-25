// info.js
// Renders the footer info pages. Each page has its own URL hash, e.g. /pages/info.html#contact

const CONTACT = {
  name: "Khaled Abdiqafar",
  email: "khalidcss19@gmail.com",
  phone: "+252711221696",
  address: "Maka Al Mukarama Road, Mogadishu, Somalia",
  hours: "Saturday to Thursday, 8:00 AM - 6:00 PM"
};

const contactCard = `
  <div class="card contact-card">
    <h4>${CONTACT.name}</h4>
    <div class="contact-row"><span>Email</span><a href="mailto:${CONTACT.email}">${CONTACT.email}</a></div>
    <div class="contact-row"><span>Phone</span><a href="tel:${CONTACT.phone}">${CONTACT.phone}</a></div>
    <div class="contact-row"><span>Address</span><span>${CONTACT.address}</span></div>
    <div class="contact-row"><span>Hours</span><span>${CONTACT.hours}</span></div>
  </div>`;

const SECTIONS = [
  { title: "Company", pages: ["about", "careers", "press"] },
  { title: "Support", pages: ["account", "help", "contact"] },
  { title: "Legals", pages: ["terms", "privacy", "licensing"] }
];

const PAGES = {
  about: {
    label: "About",
    section: "Company",
    lead: "Hospital CMS helps clinics run appointments, patient records and prescriptions from one simple system.",
    body: `
      <h3>Our story</h3>
      <p>Hospital CMS started in 2021 as a small scheduling tool for a single family clinic. Today it supports
        hospitals and private practices that want less paperwork and more time with their patients.</p>
      <div class="card-grid">
        <div class="card"><div class="stat">120+</div><p>Clinics using Hospital CMS</p></div>
        <div class="card"><div class="stat">850</div><p>Doctors managing their schedules</p></div>
        <div class="card"><div class="stat">40k</div><p>Appointments booked every month</p></div>
      </div>
      <h3>Our mission</h3>
      <p>Make quality care easier to reach by giving patients a clear way to book visits, and giving doctors
        the tools to manage their day without friction.</p>
      <h3>What we value</h3>
      <ul>
        <li><strong>Patients first:</strong> every feature starts with what patients need.</li>
        <li><strong>Privacy:</strong> medical data is handled with care and kept secure.</li>
        <li><strong>Simplicity:</strong> software should be easy for busy clinic staff to use.</li>
      </ul>`
  },
  careers: {
    label: "Careers",
    section: "Company",
    lead: "Join a small team building software that clinics rely on every day.",
    body: `
      <h3>Open positions</h3>
      <div class="card-grid">
        <div class="card"><h4>Backend Engineer (Java / Spring Boot)</h4><p class="meta">Full-time · Mogadishu or Remote</p>
          <p>Build and maintain the APIs behind appointments, prescriptions and reporting.</p></div>
        <div class="card"><h4>Frontend Developer</h4><p class="meta">Full-time · Remote</p>
          <p>Create clear, accessible dashboards for patients, doctors and administrators.</p></div>
        <div class="card"><h4>Customer Support Specialist</h4><p class="meta">Full-time · Mogadishu</p>
          <p>Help clinics get set up and answer questions from their staff.</p></div>
        <div class="card"><h4>QA Intern</h4><p class="meta">6 months · Hybrid</p>
          <p>Test new features and help us keep releases reliable.</p></div>
      </div>
      <h3>Benefits</h3>
      <ul>
        <li>Health insurance for you and your family</li>
        <li>Flexible working hours and remote options</li>
        <li>Yearly learning budget for courses and conferences</li>
        <li>25 days of paid leave</li>
      </ul>
      <h3>How to apply</h3>
      <p>Send your CV and a short note about the role you are interested in to
        <a href="mailto:${CONTACT.email}">${CONTACT.email}</a>.</p>`
  },
  press: {
    label: "Press",
    section: "Company",
    lead: "News, announcements and media resources about Hospital CMS.",
    body: `
      <h3>Latest news</h3>
      <div class="card-grid">
        <div class="card"><p class="meta">September 2025</p><h4>Hospital CMS launches online prescriptions</h4>
          <p>Doctors can now issue and store prescriptions directly from their dashboard.</p></div>
        <div class="card"><p class="meta">June 2025</p><h4>100 clinics now on Hospital CMS</h4>
          <p>The platform passes a major milestone as more clinics move away from paper records.</p></div>
        <div class="card"><p class="meta">February 2025</p><h4>New patient dashboard released</h4>
          <p>Patients can search doctors by specialty and book appointments in a few clicks.</p></div>
      </div>
      <h3>Media enquiries</h3>
      <p>For interviews, logos or product screenshots, please get in touch:</p>
      ${contactCard}`
  },
  account: {
    label: "Account",
    section: "Support",
    lead: "Everything you need to know about creating and managing your Hospital CMS account.",
    body: `
      <h3>Creating an account</h3>
      <p>Patients can sign up from the home page by choosing <strong>Patient</strong> and then <strong>Sign Up</strong>.
        You will need your name, email address, a 10-digit phone number and your home address.</p>
      <h3>Doctor and admin accounts</h3>
      <p>Doctor accounts are created by the clinic administrator. If you are a doctor and cannot log in,
        please contact your clinic's admin.</p>
      <h3>Password tips</h3>
      <ul>
        <li>Use at least 6 characters, mixing letters and numbers.</li>
        <li>Do not reuse a password from another website.</li>
        <li>Never share your password with anyone, including our support team.</li>
      </ul>
      <h3>Closing your account</h3>
      <p>To close your account or update details you cannot change yourself, email
        <a href="mailto:${CONTACT.email}">${CONTACT.email}</a> from the address registered on your account.</p>`
  },
  help: {
    label: "Help Center",
    section: "Support",
    lead: "Answers to the questions we hear most often.",
    body: `
      <div class="faq">
        <details open><summary>How do I book an appointment?</summary>
          <p>Log in as a patient, find a doctor by name or specialty, then click <strong>Book Now</strong> and pick an available time.</p></details>
        <details><summary>Can I change or cancel my appointment?</summary>
          <p>Yes. Open <strong>Appointments</strong> from the top menu, choose the appointment and use the edit option.</p></details>
        <details><summary>Where can I see my prescriptions?</summary>
          <p>Prescriptions added by your doctor are linked to your appointment and appear in your appointment history.</p></details>
        <details><summary>I forgot my password. What should I do?</summary>
          <p>Contact our support team using the details on the Contact Us page and we will help you regain access.</p></details>
        <details><summary>Is my medical information safe?</summary>
          <p>Your data is only visible to you and the doctors treating you. Read our Privacy Policy for details.</p></details>
      </div>
      <h3>Still need help?</h3>
      <p>Visit the <a href="#contact">Contact Us</a> page to reach our support team.</p>`
  },
  contact: {
    label: "Contact Us",
    section: "Support",
    lead: "Have a question, feedback or a partnership idea? We would love to hear from you.",
    body: `
      ${contactCard}
      <h3>Response times</h3>
      <p>We reply to emails within one business day. For urgent issues during working hours, please call.</p>
      <h3>Medical emergencies</h3>
      <p>Hospital CMS is a booking and records system. In a medical emergency, contact your nearest hospital
        or local emergency services immediately.</p>`
  },
  terms: {
    label: "Terms & Conditions",
    section: "Legals",
    lead: "Please read these terms carefully before using Hospital CMS.",
    body: `
      <h3>1. Using the service</h3>
      <p>By creating an account or using Hospital CMS you agree to these terms. You must provide accurate
        information and keep your login details private.</p>
      <h3>2. Appointments</h3>
      <p>Appointments are subject to the doctor's availability. Clinics may reschedule or cancel appointments
        when necessary and will try to notify you in advance.</p>
      <h3>3. Medical advice</h3>
      <p>Hospital CMS does not provide medical advice. All diagnoses and treatments are the responsibility of
        the healthcare professionals you visit.</p>
      <h3>4. Acceptable use</h3>
      <p>You must not misuse the service, attempt to access other users' data, or interfere with the system.</p>
      <h3>5. Changes</h3>
      <p>We may update these terms from time to time. Continued use of the service means you accept the updated terms.</p>
      <p class="updated">Last updated: January 1, 2026</p>`
  },
  privacy: {
    label: "Privacy Policy",
    section: "Legals",
    lead: "How we collect, use and protect your personal information.",
    body: `
      <h3>Information we collect</h3>
      <ul>
        <li>Account details such as your name, email, phone number and address.</li>
        <li>Appointment history and prescriptions added by your doctors.</li>
        <li>Basic technical data like browser type, used to keep the service working.</li>
      </ul>
      <h3>How we use it</h3>
      <p>We use your information only to provide the service: booking appointments, showing records to your
        doctors and contacting you about your account. We never sell your data.</p>
      <h3>Who can see your data</h3>
      <p>Your medical records are visible to you and to the doctors you book with. Clinic administrators can
        manage accounts but do not see prescription details.</p>
      <h3>Your rights</h3>
      <p>You can ask to see, correct or delete your personal data at any time by emailing
        <a href="mailto:${CONTACT.email}">${CONTACT.email}</a>.</p>
      <p class="updated">Last updated: January 1, 2026</p>`
  },
  licensing: {
    label: "Licensing",
    section: "Legals",
    lead: "Information about the Hospital CMS software license and third-party components.",
    body: `
      <h3>Hospital CMS license</h3>
      <p>Hospital CMS is provided to clinics under a yearly subscription license. Each license covers one clinic
        location and an unlimited number of patient accounts.</p>
      <h3>Open-source software</h3>
      <p>Hospital CMS is built with open-source software, including:</p>
      <ul>
        <li>Spring Boot (Apache License 2.0)</li>
        <li>Thymeleaf (Apache License 2.0)</li>
        <li>MySQL Connector/J (GPLv2 with FOSS exception)</li>
        <li>MongoDB Java Driver (Apache License 2.0)</li>
        <li>JJWT (Apache License 2.0)</li>
      </ul>
      <h3>Questions</h3>
      <p>For licensing or pricing questions, contact <a href="mailto:${CONTACT.email}">${CONTACT.email}</a>.</p>`
  }
};

function renderInfoNav(current) {
  document.getElementById("infoNav").innerHTML = SECTIONS.map(section => `
    <div>
      <h4>${section.title}</h4>
      ${section.pages.map(key =>
        `<a href="#${key}" class="${key === current ? "active" : ""}">${PAGES[key].label}</a>`).join("")}
    </div>`).join("");
}

function renderInfoPage() {
  let key = window.location.hash.slice(1);
  if (!PAGES[key]) key = "about";
  const page = PAGES[key];

  document.title = `${page.label} | Hospital CMS`;
  document.getElementById("infoContent").innerHTML = `
    <p class="eyebrow">${page.section}</p>
    <h1>${page.label}</h1>
    <p class="lead">${page.lead}</p>
    ${page.body}`;
  renderInfoNav(key);
  window.scrollTo(0, 0);
}

window.addEventListener("hashchange", renderInfoPage);
renderInfoPage();
