<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8" %> <% String userName = (String)
session.getAttribute("userName"); String role = (String)
session.getAttribute("role"); %> <%@ page import="java.util.*" %>

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
      .tab-content {
        display: none;
      }

      .tab-content.active {
        display: block;
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
      a {
        text-decoration: none;
        color: white;
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
      .borrow-btn {
        background-color: #66a689;
        color: white;
      }

      .drawer-overlay.visible {
        display: block;
      }
      .logout-btn {
        margin-top: "50px";
        color: #66a689;
        padding: 8px 16px;
        border: none;
        cursor: pointer;
        border-radius: 4px;
      }
    </style>
  </head>
  <body>
    <div class="dashboard">
      <aside class="sidebar">
        <h2>Member Dashboard</h2>
        <nav>
          <ul>
            <li>
              <a
                href="#"
                onclick="showTab('membership-details-tab')"
                class="tab-link"
                >Membership</a
              >
            </li>
            <li>
              <a
                href="#"
                onclick="showTab('borrowed-books-tab')"
                class="tab-link"
                >Borrowed Books</a
              >
            </li>
            <li>
              <a
                href="#"
                onclick="showTab('fines-payments-tab')"
                class="tab-link"
                >Fines & Payments</a
              >
            </li>
          </ul>
        </nav>
        <form action="logout" method="get">
          <button type="submit" class="logout-btn">Logout</button>
        </form>
      </aside>

      <main class="main-panel">
        <header>
          <h1>Welcome, <%= userName %>!</h1>
        </header>

        <!-- Membership Details -->
        <div id="membership-details-tab" class="tab-content active">
          <div class="card">
            <h3>Membership Details</h3>
            <div id="membership-details">Loading...</div>
          </div>
        </div>

        <!-- Borrowed Books -->
        <div id="borrowed-books-tab" class="tab-content">
          <div class="card">
            <div class="section-header">
              <h3>Available Books</h3>
            </div>
            <table>
              <thead>
                <tr>
                  <th>Title</th>
                  <th>Shelf</th>
                  <th>Room</th>
                  <th>Publisher</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody id="books-table-body"></tbody>
            </table>
          </div>
          <br />
          <br />
          <br />
          <div id="borrows-management">
            <div class="card">
              <div class="section-header">
                <h3>Book Borrowing Details</h3>
              </div>
              <table>
                <thead>
                  <tr>
                    <th>Book Title</th>
                    <th>Reader</th>
                    <th>Pickup Date</th>
                    <th>Due Date</th>
                    <th>Return Date</th>
                    <th>Fine</th>
                    <th>Late Charges</th>
                  </tr>
                </thead>
                <tbody id="borrows-table-body">
                  <tr>
                    <td colspan="7">Loading borrowed books...</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <!-- Fines & Payments -->
        <div id="fines-payments-tab" class="tab-content">
          <div class="card">
            <h3>Due Payments</h3>
            <p>
              <strong>Outstanding Fines:</strong>
              <span id="outstanding-fines">Loading...</span> Rwf
            </p>
          </div>
        </div>
      </main>
      >
    </div>

    <!-- Membership Request Drawer -->
    <div class="drawer-overlay" id="drawer-overlay"></div>
    <div id="membership-request-drawer" class="drawer hidden">
      <div class="drawer-content">
        <button class="close-btn" onclick="closeDrawer()">×</button>
        <h3>Request Membership</h3>
        <form id="membership-request-form">
          <label for="membership-type">Select Membership Type:</label>
          <select id="membership-type" name="membershipType"></select>
          <button onclick="sumbitMembership()" type="submit">
            Submit Request
          </button>
        </form>
      </div>
    </div>

    <script>
      // Get user data from JSP safely
      const userId = '${userId != null ? userId : ""}';
      const userName = '${userName != null ? userName : "Guest"}';
      const role = '${role != null ? role : ""}';
      const select = document.getElementById("membership-type");
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
            updateMembershipDetails(data);
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
          // Clear existing content
          membershipDetails.innerHTML = "";

          if (!memberships || memberships.length === 0) {
            const noMembershipText = document.createElement("p");
            noMembershipText.textContent =
              "No membership information available.";
            membershipDetails.appendChild(noMembershipText);

            const requestButton = document.createElement("button");
            requestButton.textContent = "Request Membership";
            requestButton.onclick = requestMembership;
            membershipDetails.appendChild(requestButton);
            return;
          }

          // Sort memberships by registrationDate in descending order
          memberships.sort((a, b) => b.registrationDate - a.registrationDate);

          // Get the latest membership
          const latestMembership = memberships[0];

          // Check if the latest membership is APPROVED
          if (latestMembership.membershipStatus !== "APPROVED") {
            const statusText = document.createElement("p");
            statusText.textContent = "Your latest membership is not approved.";
            membershipDetails.appendChild(statusText);

            const newRequestButton = document.createElement("button");
            newRequestButton.textContent = "Request New Membership";
            newRequestButton.onclick = requestMembership;
            membershipDetails.appendChild(newRequestButton);
            return;
          }

          const membershipType = latestMembership.membershipType
            ? latestMembership.membershipType.membershipName
            : "Not specified";
          const expirationDate = latestMembership.expiringTime
            ? new Date(latestMembership.expiringTime).toLocaleDateString()
            : "Not specified";

          // Create and append type information
          const typeElement = document.createElement("p");
          const typeStrong = document.createElement("strong");
          typeStrong.textContent = "Type: ";
          typeElement.appendChild(typeStrong);
          typeElement.appendChild(document.createTextNode(membershipType));
          membershipDetails.appendChild(typeElement);
          const expirationElement = document.createElement("p");
          const expirationStrong = document.createElement("strong");
          expirationStrong.textContent = "Expiration: ";
          expirationElement.appendChild(expirationStrong);
          expirationElement.appendChild(
            document.createTextNode(expirationDate)
          );
          membershipDetails.appendChild(expirationElement);

          // Create and append status information
          const statusElement = document.createElement("p");
          const statusStrong = document.createElement("strong");
          statusStrong.textContent = "Status: ";
          statusElement.appendChild(statusStrong);
          statusElement.appendChild(
            document.createTextNode(latestMembership.membershipStatus)
          );
          membershipDetails.appendChild(statusElement);
        } catch (error) {
          console.error("Error updating membership details:", error);
          membershipDetails.innerHTML = ""; // Clear existing content

          const errorText = document.createElement("p");
          errorText.textContent = "Error loading membership information.";
          membershipDetails.appendChild(errorText);

          const retryButton = document.createElement("button");
          retryButton.textContent = "Retry";
          retryButton.onclick = () => fetchMembershipData(userId);
          membershipDetails.appendChild(retryButton);
        }
      }
      function borrowThisBook(bookId) {
        fetch("borrowBook", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({ bookId }),
        })
          .then((response) => {
            if (!response.ok) {
              return response.json().then((error) => {
                throw new Error(error.error || "Failed to borrow the book");
              });
            }
            return response.json();
          })
          .then((data) => {
            alert(data.message || "Book borrowed successfully!");
            // Optionally, you could refresh the book list or update the UI
            fetchBooks();
          })
          .catch((error) => {
            console.error("Error borrowing book:", error);
            alert(`Error: ${error.error}`);
          });
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
        openDrawer();
        fetch("membership/types")
          .then((response) => {
            if (!response.ok) {
              throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
          })
          .then((types) => {
            console.log("Membership types:", types);
            if (!types || types.length === 0) {
              throw new Error("No membership types available");
            }
            for (let i = 0; i < types.length; i++) {
              const option = document.createElement("option");
              option.value = types[i].membershipTypeId;
              option.textContent = types[i].membershipName;
              select.appendChild(option);
            }
          })
          .catch((error) => {
            console.error("Error fetching membership types:", error);
            alert("Unable to load membership types. Please try again later.");
          });
      }

      // ... rest of your JavaScript functions (fetchMembershipData, handleError, drawer functions) ...
      function handleError(message) {
        const errorMessage = `
          <div class="error-message">
              <p>${message}</p>
              <button onclick="fetchMembershipData('${userId}')">Retry</button>
          </div>
      `;

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
            fetchMembershipData(userId);
            alert("Membership request submitted.");
            closeDrawer();
          });
        }
      });
      function fetchBooks() {
        fetch("books")
          .then((response) => response.json())
          .then((books) => {
            const tbody = document.getElementById("books-table-body");
            tbody.innerHTML = ""; // Clear existing rows

            if (!books || books.length === 0) {
              const noDataRow = document.createElement("tr");
              const noDataCell = document.createElement("td");
              noDataCell.setAttribute("colspan", "6");
              noDataCell.textContent = "No books available";
              noDataRow.append(noDataCell);
              tbody.append(noDataRow);
              return;
            }

            books.forEach((book) => {
              const roomCode = book.shelf.room.roomCode || "Unassigned";
              const bookCategory = book.shelf.bookCategory || "Uncategorized";
              const publisherName = book.publisherName || "Unknown";
              const bookStatus = book.bookStatus || "Unknown";

              const row = document.createElement("tr");

              const titleCell = document.createElement("td");
              titleCell.textContent = book.title;
              row.append(titleCell);

              const categoryCell = document.createElement("td");
              categoryCell.textContent = bookCategory;
              row.append(categoryCell);

              const roomCodeCell = document.createElement("td");
              roomCodeCell.textContent = roomCode;
              row.append(roomCodeCell);

              const publisherCell = document.createElement("td");
              publisherCell.textContent = publisherName;
              row.append(publisherCell);

              const statusCell = document.createElement("td");
              statusCell.textContent = bookStatus;
              row.append(statusCell);

              const actionCell = document.createElement("td");
              const borrowButton = document.createElement("button");
              borrowButton.textContent = "Borrow";
              borrowButton.className = "action-btn borrow-btn";
              borrowButton.onclick = () => borrowThisBook(book.bookId);
              actionCell.append(borrowButton);
              row.append(actionCell);

              tbody.append(row);
            });
          })
          .catch((error) => {
            console.error("Error fetching books:", error);
            const tbody = document.getElementById("books-table-body");
            tbody.innerHTML = "";
            const errorRow = document.createElement("tr");
            const errorCell = document.createElement("td");
            errorCell.setAttribute("colspan", "6");
            errorCell.textContent = "Error loading books";
            errorRow.append(errorCell);
            tbody.append(errorRow);
          });

        fetch("borrowBook", {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
          },
          credentials: "same-origin",
        })
          .then((response) => {
            if (!response.ok) {
              throw new Error("Failed to fetch borrowed books");
            }
            return response.json();
          })
          .then((borrowedBooks) => {
            const tbody = document.getElementById("borrows-table-body");
            tbody.innerHTML = ""; // Clear existing rows

            if (!borrowedBooks || borrowedBooks.length === 0) {
              const noDataRow = document.createElement("tr");
              const noDataCell = document.createElement("td");
              noDataCell.setAttribute("colspan", "7");
              noDataCell.textContent = "No borrowed books found";
              noDataRow.appendChild(noDataCell);
              tbody.appendChild(noDataRow);
              return;
            }

            borrowedBooks.forEach((borrow) => {
              const row = document.createElement("tr");

              // Book Title
              const titleCell = document.createElement("td");
              titleCell.textContent = borrow.book.title;
              row.appendChild(titleCell);

              // Reader Name
              const readerCell = document.createElement("td");
              readerCell.textContent =
                borrow.reader.firstName + " " + borrow.reader.lastName;
              row.appendChild(readerCell);

              // Pickup Date
              const pickupCell = document.createElement("td");
              pickupCell.textContent = new Date(
                borrow.pickupDate
              ).toLocaleDateString();
              row.appendChild(pickupCell);

              // Due Date
              const dueCell = document.createElement("td");
              dueCell.textContent = new Date(
                borrow.dueDate
              ).toLocaleDateString();
              row.appendChild(dueCell);

              // Return Date
              const returnCell = document.createElement("td");
              returnCell.textContent = borrow.returnDate
                ? new Date(borrow.returnDate).toLocaleDateString()
                : "Not returned";
              row.appendChild(returnCell);

              // Fine
              const fineCell = document.createElement("td");
              fineCell.textContent = borrow.fine + " Rwf";
              row.appendChild(fineCell);

              // Late Charges
              const lateChargesCell = document.createElement("td");
              lateChargesCell.textContent = borrow.lateChargeFees + "  Rwf";
              row.appendChild(lateChargesCell);

              tbody.appendChild(row);
            });
          })
          .catch((error) => {
            console.error("Error:", error);
            const tbody = document.getElementById("borrows-table-body");
            tbody.innerHTML = ""; // Clear existing rows
            const errorRow = document.createElement("tr");
            const errorCell = document.createElement("td");
            errorCell.setAttribute("colspan", "7");
            errorCell.textContent = "Error loading borrowed books";
            errorRow.appendChild(errorCell);
            tbody.appendChild(errorRow);
          });
      }

      document.addEventListener("DOMContentLoaded", () => {
        fetchBooks();
        if (userId) {
          fetchMembershipData(userId);
        } else {
          handleError("User ID is missing");
        }
      });
      // Utility function for switching tabs
      function showTab(tabId) {
        // Remove 'active' class from all tabs
        document.querySelectorAll(".tab-content").forEach((tab) => {
          tab.classList.remove("active");
        });
        const selectedTab = document.getElementById(tabId);
        if (selectedTab) {
          selectedTab.classList.add("active");
        } else {
          console.error(`Tab with ID ${tabId} not found.`);
        }
        return false;
      }

      document.addEventListener("DOMContentLoaded", () => {
        // Set default active tab on page load
        showTab("membership-details-tab");
        document.querySelectorAll(".tab-link").forEach((link) => {
          link.addEventListener("click", (event) => {
            event.preventDefault();
            const tabId = link.getAttribute("onclick").match(/'(.*?)'/)[1];
            showTab(tabId);
          });
        });
        if (userId) {
          fetchMembershipData(userId);
        } else {
          handleError("User ID is missing");
        }
      });

      function sumbitMembership() {
        console.log("Submitting membership request...");

        const membershipTypeId =
          document.getElementById("membership-type").value;

        fetch("membership", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            userId: userId,
            membershipTypeId: membershipTypeId,
            role: role,
          }),
        })
          .then((response) => {
            if (!response.ok) throw new Error("Failed to submit membership");
            return response.json();
          })
          .then((data) => {
            alert(`Request successful: ${data.message}`);
            closeDrawer();
            fetchMembershipData(userId);
          })
          .catch((err) => handleError(err.message));
      }
    </script>
  </body>
</html>
