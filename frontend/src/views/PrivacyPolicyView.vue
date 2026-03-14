<template>
  <div class="legal-page">
    <nav class="legal-nav">
      <a href="/" class="legal-nav-brand">SOCRATICA</a>
    </nav>

    <main class="legal-content">
      <h1 class="legal-title">Privacy Policy</h1>
      <p class="legal-meta">Last updated: March 14, 2026</p>

      <section class="legal-section">
        <h2>1. Introduction</h2>
        <p>Socratica ("we", "our", or "us") is committed to protecting your personal data. This Privacy Policy explains how we collect, use, store, and share information about you when you use our platform at socratica.ai and any associated services (collectively, the "Service").</p>
        <p>This policy complies with the General Data Protection Regulation (GDPR), the California Consumer Privacy Act (CCPA), and other applicable data protection laws.</p>
      </section>

      <section class="legal-section">
        <h2>2. Data Controller</h2>
        <p>The data controller responsible for your personal data is Socratica. You can contact us at:</p>
        <p><strong>Email:</strong> <a href="mailto:socratica.learn@gmail.com">socratica.learn@gmail.com</a></p>
      </section>

      <section class="legal-section">
        <h2>3. Data We Collect</h2>
        <p>We collect the following categories of personal data:</p>
        <h3>3.1 Data you provide directly</h3>
        <ul>
          <li>Account information: name, email address, password (hashed)</li>
          <li>Profile preferences: learning goals, avatar customization settings</li>
          <li>User-generated content: files you upload, notes, quiz responses</li>
          <li>Communications: messages you send to our support team</li>
        </ul>
        <h3>3.2 Data collected automatically</h3>
        <ul>
          <li>Usage data: pages visited, features used, session duration</li>
          <li>Device data: browser type, operating system, IP address</li>
          <li>Cookies and similar tracking technologies (see our Cookie Policy)</li>
        </ul>
        <h3>3.3 Data from third parties</h3>
        <ul>
          <li>Authentication data when you sign in via Google OAuth</li>
        </ul>
      </section>

      <section class="legal-section">
        <h2>4. How We Use Your Data</h2>
        <p>We process your personal data for the following purposes and on the following legal bases:</p>
        <table class="legal-table">
          <thead>
            <tr><th>Purpose</th><th>Legal Basis</th></tr>
          </thead>
          <tbody>
            <tr><td>Providing and improving the Service</td><td>Performance of contract</td></tr>
            <tr><td>Personalising your learning experience</td><td>Legitimate interests</td></tr>
            <tr><td>Sending service-related communications</td><td>Performance of contract</td></tr>
            <tr><td>Analytics and product improvement</td><td>Legitimate interests</td></tr>
            <tr><td>Legal compliance and fraud prevention</td><td>Legal obligation</td></tr>
            <tr><td>Marketing communications (optional)</td><td>Consent</td></tr>
          </tbody>
        </table>
      </section>

      <section class="legal-section">
        <h2>5. Data Sharing</h2>
        <p>We do not sell your personal data. We may share your data with:</p>
        <ul>
          <li><strong>Service providers:</strong> cloud infrastructure (e.g. Google Cloud), analytics tools, and AI model providers (e.g. Google Gemini API) — under strict data processing agreements</li>
          <li><strong>Legal authorities:</strong> when required by law, court order, or to protect our legal rights</li>
          <li><strong>Business transfers:</strong> in the event of a merger, acquisition, or sale of assets, your data may be transferred with appropriate notice</li>
        </ul>
      </section>

      <section class="legal-section">
        <h2>6. Data Retention</h2>
        <p>We retain your personal data for as long as your account is active or as needed to provide the Service. You may request deletion of your account and associated data at any time by contacting us. We will comply within 30 days unless we are required by law to retain certain information.</p>
      </section>

      <section class="legal-section">
        <h2>7. Your Rights</h2>
        <p>Depending on your jurisdiction, you may have the following rights regarding your personal data:</p>
        <ul>
          <li><strong>Right of access:</strong> request a copy of the data we hold about you</li>
          <li><strong>Right to rectification:</strong> correct inaccurate or incomplete data</li>
          <li><strong>Right to erasure:</strong> request deletion of your data ("right to be forgotten")</li>
          <li><strong>Right to restriction:</strong> ask us to limit how we process your data</li>
          <li><strong>Right to portability:</strong> receive your data in a structured, machine-readable format</li>
          <li><strong>Right to object:</strong> object to processing based on legitimate interests or for direct marketing</li>
          <li><strong>Right to withdraw consent:</strong> where processing is based on consent, withdraw it at any time</li>
        </ul>
        <p>To exercise any of these rights, contact us at <a href="mailto:socratica.learn@gmail.com">socratica.learn@gmail.com</a>.</p>
      </section>

      <section class="legal-section">
        <h2>8. International Transfers</h2>
        <p>Your data may be processed in countries outside your own, including the United States. Where we transfer data internationally, we ensure appropriate safeguards are in place (e.g. Standard Contractual Clauses approved by the European Commission).</p>
      </section>

      <section class="legal-section">
        <h2>9. Security</h2>
        <p>We implement industry-standard security measures including encryption in transit (TLS) and at rest, access controls, and regular security reviews. However, no method of transmission over the internet is 100% secure, and we cannot guarantee absolute security.</p>
      </section>

      <section class="legal-section">
        <h2>10. Children's Privacy</h2>
        <p>The Service is not directed to children under the age of 16. We do not knowingly collect personal data from children. If you believe we have collected data from a child, please contact us immediately.</p>
      </section>

      <section class="legal-section">
        <h2>11. Changes to This Policy</h2>
        <p>We may update this Privacy Policy from time to time. We will notify you of significant changes by email or via a prominent notice on our platform. Your continued use of the Service after changes take effect constitutes acceptance of the updated policy.</p>
      </section>

      <section class="legal-section">
        <h2>12. Contact</h2>
        <p>For any questions or concerns about this Privacy Policy, please contact us at <a href="mailto:socratica.learn@gmail.com">socratica.learn@gmail.com</a>.</p>
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
