# Internet Provider Comparison Tool

This project provides a fast and user-friendly way to compare internet offers from different providers in Germany.

## 🔗 Access the Project

- **Live App**: [https://frontend-253516409283.europe-west1.run.app/](https://frontend-253516409283.europe-west1.run.app/)
- **Docker**: Run the application locally with:
  ```bash
  docker compose up
  ```
  Pls note that you need keys to access the apis. Create a .env file and parse them there.

## ☁️ Hosting & Infrastructure

- **Frontend & Backend**: Deployed on **Google Cloud Run**
- **Database**: Hosted on **[Neon.tech](https://neon.tech/)** (PostgreSQL)

---

## 🚀 Key Features and Architecture

### Streaming API Results for Better UX

To improve user experience and avoid delays caused by APIs returning results one by one, **Server-Sent Events (SSE)** were implemented in combination with **Project Reactor’s Flux** (Spring WebFlux).

- This allows streaming each individual result to the frontend as soon as it arrives, without waiting for all providers to respond.
- The result: a much more responsive experience for the user.

### ServusSpeed: Early-Stage Optimization (Not Used)

Initially, the ServusSpeed API experienced extreme delays (20s+). To tackle this, I developed a feature called **UFO (Unverified Fallback Offers)**:
- Cached results from the database were immediately shown to users.
- Once actual results arrived, they would replace the cached ones.

However, by the time implementation neared completion, **Check24 had optimized the API**, reducing delays to ~2–5 seconds, making UFO unnecessary. While this feature was not shipped, it consumed significant development time. I completely removed the implementation.

### Unified Data Mapping & Parallel Execution

Each provider has its unique:
- Data format
- Access requirements
- Timeout behaviors

To standardize these:
- **ResponseDTO** format ensures a consistent output to the frontend.
- **Individual mappers and model classes** are created per provider.
- A **dedicated client class** handles API communication.
- Each provider implements the `OfferProvider` interface.
- The `OfferServiceReactive` combines all providers and ensures **parallel** data fetching.

### Filtering & Sorting

- Before sending offers to the frontend, we **filter based on**:
  - Price range
  - TV options
  - Installation service
  - Connection type

- Sorting (by price, speed, or user rating) is **handled in the frontend**.
  - Since results stream one-by-one and aren't overwhelming in volume, client-side sorting is simpler and more efficient.

### Session-Based Results Sharing

- Each user search generates a **unique Session ID**.
- All offers retrieved are stored in the PostgreSQL DB under this ID.
- Users can **share** results via this ID.
  - Anyone with the link can reload the saved offers using the same ID.

### User Ratings & Comments

- Users can **rank offers**, matched via the **exact offer name**.
- Rankings are attached during search result preparation.
- **Comments** are **lazy-loaded** only on demand, reducing unnecessary data transfer.

### Security & Configuration

- All sensitive credentials are stored as **environment variables**.
- Managed using `application.yml` (excluded from Git for security reasons).

---

## 🏙 Address Autocompletion

To simplify address entry for users:

- **PLZ + City Autocomplete**: Uses `opendatasoft` with `georef-germany-postleitzahl` dataset.
  - Returns combined string like `"80797 München"` for substring matching.

- **Street Autocomplete**: Uses **Photon API (by Komoot)** in combination with retrieved PLZ and City.
  - **Note**: I didn't implement street **number** autocompletion.


