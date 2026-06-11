<div align="center">
  <img src="https://img.shields.io/badge/Java 17-000000?style=for-the-badge&logo=openjdk&logoColor=white">
  <img src="https://img.shields.io/badge/D3-F9A03C?style=for-the-badge&logo=D3&logoColor=white">
  <img src="https://img.shields.io/badge/Apache FreeMarker-326CAC?style=for-the-badge&logo=apachefreemarker&logoColor=white">
  <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white">
  <img src="https://img.shields.io/badge/LaTeX-008080?style=for-the-badge&logo=latex&logoColor=white">
  <img src="https://img.shields.io/badge/MongoDB-47A248?style=for-the-badge&logo=mongodb&logoColor=white">
  <img src="https://img.shields.io/badge/License-MIT-green?style=for-the-badge">
</div>

<br />

<p align="center">
  <img src="assets/icon.png" alt="Logo" width="120" height="120" style="border-radius:15%;">
</p>
<h3 align="center">Multimodal Parliament Explorer</h3>
<p align="center">Final project of the programming lab — visualizing and analyzing German Bundestag plenary protocols.</p>

<div align="center">
  <a href="https://autokaiai.github.io/multimodal_parliament_explorer/">
    <img alt="Open JavaDoc" src="https://img.shields.io/badge/Open JavaDoc-F89820?style=for-the-badge&logo=openjdk&logoColor=white">
  </a>
  &nbsp;
  <a href="benutzerhandbuch.md">
    <img alt="User Manual" src="https://img.shields.io/badge/User Manual-000000?style=for-the-badge&logo=markdown&logoColor=white">
  </a>
</div>

---

> [!NOTE]
> **Group project.** This is the repository of our group project, which all four team members contributed to. It was first hosted at `github.com/Kuuhhl/multimodal_parliament_explorer`.

## About the project

The **Multimodal Parliament Explorer** makes speeches from the German Bundestag searchable and analyzable. Plenary protocols are imported, enriched with NLP techniques, and presented together with the corresponding video. Results can be visualized interactively and exported as XML or PDF.

## Features

* View parliamentary speeches alongside their video
* Information about speakers and protocols
* Visualization of NLP analyses through interactive charts (D3)
* Full-text and advanced search
* Export as XML / PDF (via LaTeX)

## Tech stack

Java 17 · Javalin · Apache FreeMarker · MongoDB · D3.js · LaTeX · Docker

## Requirements

* Git
* Docker

## Usage (with Docker)

1. Clone the repository: `git clone https://github.com/autokaiai/multimodal_parliament_explorer`
2. Enter the directory: `cd multimodal_parliament_explorer`
3. Build the Docker image: `docker build -t multimodal_parliament_explorer .`
4. Start the container: `docker run --rm -d -p 7001:7001 multimodal_parliament_explorer`
5. Open in your browser: [http://localhost:7001](http://localhost:7001)

> [!TIP]
> For more detailed instructions, see the [user manual](benutzerhandbuch.md).

## Screenshots

<div align="center">
  <img src="assets/screenshot-1.png" alt="Screenshot 1" width="300" style="margin-right: 20px;">
  <img src="assets/screenshot-2.png" alt="Screenshot 2" width="300" style="margin-left: 20px;">
</div>

## Team

This project was developed collaboratively by the following four people:

* Philipp Hein
* Philipp Landmann
* Philip Schneider
* Kai Alois Wöllstein

## License & citation

Released under the [MIT License](LICENSE).

Any use, reuse, or distribution must credit **all four team members** (Philipp Hein, Philipp Landmann, Philip Schneider, and Kai Alois Wöllstein). The full license and copyright notice from the [`LICENSE`](LICENSE) file must be retained.
