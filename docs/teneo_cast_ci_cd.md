## ⚙️ CI/CD Strategy

### Objectives

- Build and deploy each interface (Player, Studio, Console)
- Automate platform backend testing and packaging
- Deploy Web interfaces as static files to a CDN
- Release Windows and Android Player builds on demand or via changes
- Generate and host full system documentation automatically

---

### 🧩 Directory Structure (Partial Example)

```
/teneocast
├── apps
│   ├── player-web/
│   ├── player-windows/
│   ├── player-android/
│   ├── studio/
│   └── console/
├── backend/
├── docs/
└── .github/workflows/
```

---

### 🏗 GitHub Actions Workflows

#### 1. **Build Player (Web)**

Trigger: Push to `apps/player-web/`

- Run `flutter build web`
- Upload to S3/CDN bucket (e.g., CloudFront or Firebase Hosting)

#### 2. **Build Studio & Console (Web)**

Trigger: Push to `apps/studio/` or `apps/console/`

- Run `flutter build web`
- Upload to corresponding subdomain:
  - `studio.teneocast.com`
  - `console.teneocast.com`

#### 3. **Build Player (Windows)**

Trigger: Push to `apps/player-windows/`

- Use `flutter build windows`
- Upload executable to GitHub Releases or Artifacts

#### 4. **Build Player (Android)**

Trigger: Push to `apps/player-android/`

- Use `flutter build apk`
- Upload APK to GitHub Releases (eventually to Play Store CI)

#### 5. **Backend Pipeline**

Trigger: Push to `backend/`

- Run `./gradlew test`
- Run Testcontainers integration tests
- Build Docker images for each service
- Push to ECR (for Fargate deployment)

#### 6. **Documentation Deployment**

Trigger: Push to `docs/` or merge into `main`

- Generate static site (e.g., using MkDocs or Docusaurus)
- Deploy to GitHub Pages at `teneocast.github.io/docs`
- Link from Console or Studio as "Developer Docs"

---

### 📦 CDNs & Hosting

- S3 + CloudFront for Web Player, Studio, Console
- GitHub Pages for Docs
- ECR + Fargate for backend services
- APK / EXE served through GitHub Releases (initially)

---

### 🛡 Good Practices

- Branch protections with required status checks
- PRs trigger preview deployments (optional with unique URL)
- Automated tests must pass for deploys
- Use caching for Flutter & Gradle builds

---

Next: Define PR templates, changelog strategy, and versioning conventions (SemVer?)

