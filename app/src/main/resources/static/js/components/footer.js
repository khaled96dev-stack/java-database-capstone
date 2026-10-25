// footer.js

function renderFooter() {
  const footer = document.getElementById("footer");
  if (!footer) return;

  footer.innerHTML = `
    <footer class="footer">
      <div class="footer-container">
        <div class="footer-logo">
          <img src="../assets/images/logo/logo.png" alt="Hospital CMS Logo">
          <p>&copy; Copyright 2025. All Rights Reserved by Hospital CMS.</p>
        </div>
        <div class="footer-links">
          <div class="footer-column">
            <h4>Company</h4>
            <a href="/pages/info.html#about">About</a>
            <a href="/pages/info.html#careers">Careers</a>
            <a href="/pages/info.html#press">Press</a>
          </div>
          <div class="footer-column">
            <h4>Support</h4>
            <a href="/pages/info.html#account">Account</a>
            <a href="/pages/info.html#help">Help Center</a>
            <a href="/pages/info.html#contact">Contact Us</a>
          </div>
          <div class="footer-column">
            <h4>Legals</h4>
            <a href="/pages/info.html#terms">Terms &amp; Conditions</a>
            <a href="/pages/info.html#privacy">Privacy Policy</a>
            <a href="/pages/info.html#licensing">Licensing</a>
          </div>
        </div>
      </div>
    </footer>`;
}

renderFooter();
