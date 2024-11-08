<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8" %> <%@ page import="java.util.*" %>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Member Dashboard</title>
    <link rel="stylesheet" href="./styles/member.css" />
    <style>
      /* Drawer Styles */
      .drawer {
        position: fixed;
        top: 0;
        right: 0;
        width: 300px;
        height: 100%;
        background-color: #f8f8f8;
        box-shadow: -2px 0 5px rgba(0, 0, 0, 0.1);
        padding: 20px;
        display: flex;
        flex-direction: column;
        justify-content: start;
        transform: translateX(100%);
        transition: transform 0.3s ease;
        z-index: 1000;
      }

      .drawer-content {
        margin-top: 20px;
      }

      .hidden {
        display: none;
      }

      .drawer.open {
        transform: translateX(0);
      }

      .close-btn {
        background: none;
        border: none;
        font-size: 24px;
        align-self: flex-end;
        cursor: pointer;
      }

      #membership-request-form {
        display: flex;
        flex-direction: column;
      }

      #membership-request-form label,
      #membership-request-form select,
      #membership-request-form button {
        margin-bottom: 10px;
      }

      #membership-request-form button {
        background-color: #007bff;
        color: white;
        border: none;
        padding: 10px;
        cursor: pointer;
        border-radius: 5px;
      }

      #membership-request-form button:hover {
        background-color: #0056b3;
      }

      .drawer-overlay {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0, 0, 0, 0.5);
        display: none;
        z-index: 999;
      }

      .drawer-overlay.visible {
        display: block;
      }
    </style>
  </head>
  <body>
    <div class="dashboard">
      <aside class="sidebar">
        <h2>Member Dashboard</h2>
        <nav>
          <ul>
            <li><a href="#membership-status">My Membership</a></li>
            <li><a href="#borrowed-books">Borrowed Books</a></li>
            <li><a href="#fines-payments">Fines & Payments</a></li>
          </ul>
        </nav>
      </aside>

      <main class="main-panel">
        <header>
          <h1>Welcome, ${userName != null ? userName : 'Guest'}!</h1>
        </header>

        <section class="summary-cards">
          <div class="card" id="membership-status">
            <h3>Membership Details</h3>
            <div id="membership-details">
              <p>Loading membership details...</p>
            </div>
          </div>

          <div class="card" id="borrowed-books">
            <h3>Borrowed Books</h3>
            <table>
              <thead>
                <tr>
                  <th>Title</th>
                  <th>Due Date</th>
                  <th>Fine</th>
                </tr>
              </thead>
              <tbody id="borrowed-books-list">
                <tr>
                  <td colspan="3">Loading...</td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="card" id="fines-payments">
            <h3>Due Payments</h3>
            <p>
              <strong>Outstanding Fines:</strong>
              <span id="outstanding-fines">Loading...</span> Rwf
            </p>
          </div>
        </section>
      </main>
    </div>

    <!-- Membership Request Drawer -->
    <div class="drawer-overlay" id="drawer-overlay"></div>
    <div id="membership-request-drawer" class="drawer hidden">
      <div class="drawer-content">
        <button class="close-btn" onclick="closeDrawer()">×</button>
        <h3>Request Membership</h3>
        <form id="membership-request-form">
          <label for="membership-type">Select Membership Type:</label>
          <select id="membership-type" name="membershipType">
            <option value="">Loading membership types...</option>
          </select>
          <button type="submit">Submit Request</button>
        </form>
      </div>
    </div>

    <script>
      // Get user data from JSP safely
      const userId = '${userId != null ? userId : ""}';
      const userName = '${userName != null ? userName : "Guest"}';
      function fetchMembershipData(userId) {
        if (!userId) {
          handleError("User ID is missing");
          return;
        }

        fetch(`membership?userId=${userId}`)
          .then((response) => {
            if (!response.ok) {
              throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
          })
          .then((data) => {
            updateMembershipDetails(data?.memberships);
            updateBorrowedBooks(data?.borrowedBooks);
            // updateOutstandingFines(data?.outstandingFines);
          })
          .catch((error) => {
            console.error("Error fetching data:", error);
            handleError("Failed to load dashboard data");
          });
      }
      function updateMembershipDetails(memberships) {
        const membershipDetails = document.getElementById("membership-details");
        if (!membershipDetails) return;

        try {
          if (!memberships || memberships.length === 0) {
            membershipDetails.innerHTML = `
                        <p>No membership information available.</p>
                        <button onclick="requestMembership()">Request Membership</button>
                    `;
            return;
          }

          const membership = memberships[0];
          if (!membership || membership.membershipStatus === "EXPIRED") {
            membershipDetails.innerHTML = `
                        <p>Your membership has expired.</p>
                        <button onclick="requestMembership()">Renew Membership</button>
                    `;
            return;
          }

          const membershipType = membership.membershipType
            ? membership.membershipType.name
            : "Standard";
          const expirationDate = membership.expiringTime
            ? new Date(membership.expiringTime).toLocaleDateString()
            : "Not specified";

          membershipDetails.innerHTML = `
                    <p><strong>Type:</strong> ${membershipType}</p>
                    <p><strong>Expiration:</strong> ${expirationDate}</p>
                `;
        } catch (error) {
          console.error("Error updating membership details:", error);
          membershipDetails.innerHTML = `
                    <p>Error loading membership information.</p>
                    <button onclick="fetchMembershipData('${userId}')">Retry</button>
                `;
        }
      }

      function updateBorrowedBooks(books) {
        const booksList = document.getElementById("borrowed-books-list");
        if (!booksList) return;

        try {
          if (!books || books.length === 0) {
            booksList.innerHTML = `
                        <tr>
                            <td colspan="3">No books currently borrowed</td>
                        </tr>
                    `;
            return;
          }

          booksList.innerHTML = books
            .map((book) => {
              const title = book && book.title ? book.title : "Unknown Title";
              const dueDate =
                book && book.dueDate
                  ? new Date(book.dueDate).toLocaleDateString()
                  : "Not specified";
              const fine = book && book.fine ? `${book.fine} Rwf` : "0 Rwf";

              return `
                        <tr>
                            <td>${title}</td>
                            <td>${dueDate}</td>
                            <td>${fine}</td>
                        </tr>
                    `;
            })
            .join("");
        } catch (error) {
          console.error("Error updating borrowed books:", error);
          booksList.innerHTML = `
                    <tr>
                        <td colspan="3">Error loading borrowed books</td>
                    </tr>
                `;
        }
      }

      function requestMembership() {
        fetch("membership/types")
          .then((response) => {
            if (!response.ok) {
              throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
          })
          .then((types) => {
            if (!types || types.length === 0) {
              throw new Error("No membership types available");
            }

            const select = document.getElementById("membership-type");
            if (!select) return;

            select.innerHTML = types
              .map((type) => {
                const typeId = type ? type._id || "" : "";
                const typeName = type
                  ? type.name || "Unknown Type"
                  : "Unknown Type";
                return `<option value="${typeId}">${typeName}</option>`;
              })
              .join("");
            openDrawer();
          })
          .catch((error) => {
            console.error("Error fetching membership types:", error);
            alert("Unable to load membership types. Please try again later.");
          });
      }

      // ... rest of your JavaScript functions (fetchMembershipData, handleError, drawer functions) ...
      function handleError(message) {
        // You can customize this function to show errors in a user-friendly way
        const errorMessage = `
        <div class="error-message">
            <p>${message}</p>
            <button onclick="fetchMembershipData('${userId}')">Retry</button>
        </div>
    `;

        // Update each section with error state
        const sections = [
          "membership-details",
          "borrowed-books-list",
          "outstanding-fines",
        ];
        sections.forEach((id) => {
          const element = document.getElementById(id);
          if (element) {
            if (id === "borrowed-books-list") {
              element.innerHTML = `<tr><td colspan="3">${message}</td></tr>`;
            } else if (id === "outstanding-fines") {
              element.textContent = "Error";
            } else {
              element.innerHTML = errorMessage;
            }
          }
        });
      }

      // Utility function for drawer
      function openDrawer() {
        const overlay = document.getElementById("drawer-overlay");
        const drawer = document.getElementById("membership-request-drawer");

        if (!overlay || !drawer) return;

        overlay.classList.add("visible");
        drawer.classList.remove("hidden");
        drawer.classList.add("open");
      }

      function closeDrawer() {
        const overlay = document.getElementById("drawer-overlay");
        const drawer = document.getElementById("membership-request-drawer");

        if (!overlay || !drawer) return;

        overlay.classList.remove("visible");
        drawer.classList.remove("open");
        setTimeout(() => drawer.classList.add("hidden"), 300);
      }

      // Event Listeners
      document.addEventListener("DOMContentLoaded", () => {
        const overlay = document.getElementById("drawer-overlay");
        const form = document.getElementById("membership-request-form");

        if (overlay) {
          overlay.addEventListener("click", closeDrawer);
        }

        if (form) {
          form.addEventListener("submit", function (event) {
            event.preventDefault();
            alert("Membership request submitted.");
            closeDrawer();
          });
        }

        // Initial data fetch
        fetchMembershipData(userId);
      });
      // Initial data fetch
      document.addEventListener("DOMContentLoaded", () => {
        if (userId) {
          fetchMembershipData(userId);
        } else {
          handleError("User ID is missing");
        }
      });
    </script>
  </body>
</html>
