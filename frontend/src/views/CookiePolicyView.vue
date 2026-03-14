<template>
  <div class="legal-page">
    <nav class="legal-nav">
      <a href="/" class="legal-nav-brand">SOCRATICA</a>
    </nav>

    <main class="legal-content">
      <h1 class="legal-title">Cookie Policy</h1>
      <p class="legal-meta">Last updated: March 14, 2026</p>

      <section class="legal-section">
        <h2>1. What Are Cookies?</h2>
        <p>Cookies are small text files placed on your device (computer, smartphone, or tablet) when you visit a website. They are widely used to make websites work efficiently, remember your preferences, and provide analytical information to website owners.</p>
        <p>Similar technologies include web beacons, pixels, and local storage — this policy covers all such technologies collectively referred to as "cookies".</p>
      </section>

      <section class="legal-section">
        <h2>2. How We Use Cookies</h2>
        <p>Socratica uses cookies to:</p>
        <ul>
          <li>Keep you signed in to your account across sessions</li>
          <li>Remember your preferences and personalisation settings</li>
          <li>Understand how you use the Service so we can improve it</li>
          <li>Ensure the security of your account</li>
        </ul>
      </section>

      <section class="legal-section">
        <h2>3. Types of Cookies We Use</h2>

        <h3>3.1 Strictly Necessary Cookies</h3>
        <p>These cookies are essential for the Service to function and cannot be disabled. They include:</p>
        <table class="legal-table">
          <thead>
            <tr><th>Cookie</th><th>Purpose</th><th>Duration</th></tr>
          </thead>
          <tbody>
            <tr><td>session_token</td><td>Authenticates your login session</td><td>Session</td></tr>
            <tr><td>csrf_token</td><td>Protects against cross-site request forgery</td><td>Session</td></tr>
          </tbody>
        </table>

        <h3>3.2 Functional Cookies</h3>
        <p>These cookies remember your preferences and personalisation choices:</p>
        <table class="legal-table">
          <thead>
            <tr><th>Cookie</th><th>Purpose</th><th>Duration</th></tr>
          </thead>
          <tbody>
            <tr><td>user_prefs</td><td>Stores language and display preferences</td><td>1 year</td></tr>
            <tr><td>avatar_config</td><td>Remembers your avatar customisation</td><td>1 year</td></tr>
          </tbody>
        </table>

        <h3>3.3 Analytics Cookies</h3>
        <p>These cookies help us understand how visitors interact with our platform. All data is aggregated and anonymised:</p>
        <table class="legal-table">
          <thead>
            <tr><th>Cookie</th><th>Provider</th><th>Purpose</th><th>Duration</th></tr>
          </thead>
          <tbody>
            <tr><td>_ga</td><td>Google Analytics</td><td>Distinguishes unique users</td><td>2 years</td></tr>
            <tr><td>_ga_*</td><td>Google Analytics</td><td>Maintains session state</td><td>2 years</td></tr>
          </tbody>
        </table>
      </section>

      <section class="legal-section">
        <h2>4. Third-Party Cookies</h2>
        <p>Some features of our Service are provided by third parties who may set their own cookies:</p>
        <ul>
          <li><strong>Google OAuth:</strong> used for sign-in functionality — governed by <a href="https://policies.google.com/privacy" target="_blank" rel="noopener">Google's Privacy Policy</a></li>
          <li><strong>Google Analytics:</strong> used for usage analytics — you can opt out via <a href="https://tools.google.com/dlpage/gaoptout" target="_blank" rel="noopener">Google's opt-out tool</a></li>
          <li><strong>Spline:</strong> used for 3D interactive elements — governed by Spline's privacy policy</li>
        </ul>
      </section>

      <section class="legal-section">
        <h2>5. Managing Your Cookie Preferences</h2>
        <p>You can control and manage cookies in several ways:</p>

        <h3>5.1 Browser settings</h3>
        <p>Most browsers allow you to refuse or delete cookies through their settings. Note that disabling strictly necessary cookies will affect the functionality of the Service. Links to browser cookie controls:</p>
        <ul>
          <li><a href="https://support.google.com/chrome/answer/95647" target="_blank" rel="noopener">Google Chrome</a></li>
          <li><a href="https://support.mozilla.org/en-US/kb/enable-and-disable-cookies-website-preferences" target="_blank" rel="noopener">Mozilla Firefox</a></li>
          <li><a href="https://support.apple.com/guide/safari/manage-cookies-sfri11471" target="_blank" rel="noopener">Apple Safari</a></li>
          <li><a href="https://support.microsoft.com/en-us/microsoft-edge/delete-cookies-in-microsoft-edge" target="_blank" rel="noopener">Microsoft Edge</a></li>
        </ul>

        <h3>5.2 Opt-out tools</h3>
        <p>You can opt out of interest-based advertising via the <a href="https://www.youronlinechoices.com" target="_blank" rel="noopener">Your Online Choices</a> platform (EU) or the <a href="https://optout.aboutads.info" target="_blank" rel="noopener">Digital Advertising Alliance</a> opt-out tool (US).</p>
      </section>

      <section class="legal-section">
        <h2>6. Changes to This Policy</h2>
        <p>We may update this Cookie Policy periodically. Any changes will be posted on this page with an updated "Last updated" date. We encourage you to review this policy regularly.</p>
      </section>

      <section class="legal-section">
        <h2>7. Contact</h2>
        <p>If you have questions about our use of cookies, please contact us at <a href="mailto:socratica.learn@gmail.com">socratica.learn@gmail.com</a>.</p>
      </section>
    </main>

    <footer class="legal-footer">
      <p>© {{ new Date().getFullYear() }} Socratica. All rights reserved.</p>
      <div class="legal-footer-links">
        <router-link to="/privacy-policy">Privacy Policy</router-link>
        <router-link to="/cookie-policy">Cookie Policy</router-link>
        <router-link to="/terms-of-service">Terms of Service</router-link>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Red+Hat+Display:wght@400;500;600;700&display=swap");

.legal-page {
  min-height: 100vh;
  background: #F7F7F2;
  font-family: 'Red Hat Display', sans-serif;
  color: #1a1a1a;
  display: flex;
  flex-direction: column;
}

.legal-nav {
  position: sticky;
  top: 0;
  background: #F7F7F2;
  border-bottom: 1px solid rgba(0,0,0,0.08);
  padding: 1.25rem clamp(2rem, 6vw, 5rem);
  z-index: 10;
}

.legal-nav-brand {
  font-family: "Times New Roman", "Times", serif;
  font-size: 1rem;
  letter-spacing: 0.22em;
  color: #1a1a1a;
  text-decoration: none;
  opacity: 0.85;
}

.legal-content {
  max-width: 800px;
  width: 100%;
  margin: 0 auto;
  padding: clamp(3rem, 6vw, 5rem) clamp(2rem, 5vw, 3rem);
  flex: 1;
}

.legal-title {
  font-family: "Times New Roman", "Times", serif;
  font-size: clamp(2rem, 4vw, 3rem);
  font-weight: 400;
  letter-spacing: 0.04em;
  color: #1a1a1a;
  margin-bottom: 0.5rem;
}

.legal-meta {
  font-size: 0.85rem;
  color: rgba(0,0,0,0.4);
  margin-bottom: 3rem;
  border-bottom: 1px solid rgba(0,0,0,0.08);
  padding-bottom: 1.5rem;
}

.legal-section {
  margin-bottom: 2.5rem;
}

.legal-section h2 {
  font-family: "Times New Roman", "Times", serif;
  font-size: clamp(1.1rem, 2vw, 1.4rem);
  font-weight: 400;
  color: #1a1a1a;
  margin-bottom: 0.75rem;
  margin-top: 0;
}

.legal-section h3 {
  font-size: 0.95rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 1rem 0 0.5rem;
}

.legal-section p {
  font-size: clamp(0.9rem, 1.4vw, 1rem);
  line-height: 1.8;
  color: rgba(0,0,0,0.75);
  margin-bottom: 0.75rem;
}

.legal-section ul {
  padding-left: 1.5rem;
  margin-bottom: 0.75rem;
}

.legal-section li {
  font-size: clamp(0.9rem, 1.4vw, 1rem);
  line-height: 1.8;
  color: rgba(0,0,0,0.75);
  margin-bottom: 0.25rem;
}

.legal-section a {
  color: #cb9b51;
  text-decoration: none;
}

.legal-section a:hover {
  text-decoration: underline;
}

.legal-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
  margin-top: 0.75rem;
  margin-bottom: 1rem;
}

.legal-table th {
  background: rgba(0,0,0,0.04);
  padding: 0.6rem 1rem;
  text-align: left;
  font-weight: 600;
  border: 1px solid rgba(0,0,0,0.1);
}

.legal-table td {
  padding: 0.6rem 1rem;
  border: 1px solid rgba(0,0,0,0.1);
  color: rgba(0,0,0,0.75);
  line-height: 1.6;
}

.legal-footer {
  background: #F7F7F2;
  border-top: 1px solid rgba(0,0,0,0.08);
  padding: 1.5rem clamp(2rem, 6vw, 5rem);
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 1rem;
}

.legal-footer p {
  font-size: 0.8rem;
  color: rgba(0,0,0,0.35);
  margin: 0;
}

.legal-footer-links {
  display: flex;
  gap: 1.5rem;
}

.legal-footer-links a {
  font-size: 0.8rem;
  color: rgba(0,0,0,0.35);
  text-decoration: none;
  transition: color 0.2s ease;
}

.legal-footer-links a:hover {
  color: #cb9b51;
}
</style>
