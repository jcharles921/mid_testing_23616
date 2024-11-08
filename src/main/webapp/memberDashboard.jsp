<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.text.SimpleDateFormat, java.util.Date" %>
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

      .drawer h3 {
        margin-top: 0;
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
    </style>
  </head>
  <body>
    <div class="dashboard">
      <!-- Sidebar -->
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

      <!-- Main Panel -->
      <main class="main-panel">
        <header>
          <h1>Welcome, <%= session.getAttribute("userName") %>!</h1>
        </header>

        <section class="summary-cards">
          <!-- Membership Status -->
          <div class="card" id="membership-status">
            <h3>Membership Details</h3>
            <div id="membership-details">
              <p>Loading membership details...</p>
            </div>
          </div>

          <!-- Borrowed Books -->
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
                  <td>Loading...</td>
                  <td></td>
                  <td></td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- Fines and Payments -->
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
    <div id="membership-request-drawer" class="drawer hidden">
      <div class="drawer-content">
        <button class="close-btn" onclick="closeDrawer()">×</button>
        <h3>Request Membership</h3>
        <p>Fill out the form below to request a new membership.</p>
        <form id="membership-request-form">
          <label for="membership-type">Select Membership Type:</label>
          <select id="membership-type" name="membershipType">
            <option value="standard">Standard</option>
            <option value="premium">Premium</option>
          </select>
          <button type="submit">Submit Request</button>
        </form>
      </div>
    </div>

    <script>
      const userId = '<%= session.getAttribute("userId") %>';
      const userName = '<%= session.getAttribute("userName") %>';

      // Fetch and update data
      fetchMembershipData(userId);

      function fetchMembershipData(userId) {
        fetch(`membership?userId=${userId}`)
          .then((response) => response.json())
          .then((data) => {
            console.log(data);
            updateMembershipDetails(data.memberships);
            updateBorrowedBooks(data.borrowedBooks);
            updateOutstandingFines(data.outstandingFines);
          })
          .catch((error) => {
            console.error("Error fetching membership data:", error);
          });
      }

      function updateMembershipDetails(memberships) {
        const membershipDetails = document.getElementById("membership-details");

        if (
          memberships.length === 0 ||
          memberships[0].membershipStatus === "EXPIRED"
        ) {
          membershipDetails.innerHTML = `
            <p>You don't have an active membership or your membership has expired.</p>
            <button onclick="requestMembership()">Request Membership</button>
          `;
        } else {
          const membership = memberships[0];
          const expirationDate = new Date(
            membership.expiringTime
          ).toLocaleDateString();
          membershipDetails.innerHTML = `
            <p><strong>Type:</strong> ${membership.membershipType.name}</p>
            <p><strong>Expiration:</strong> ${expirationDate}</p>
            <p><strong>Borrowing Limit:</strong> ${membership.membershipType.maxBooks} books</p>
          `;
        }
      }

      function updateBorrowedBooks(borrowedBooks) {
        const booksList = document.getElementById("borrowed-books-list");
        if (borrowedBooks.length === 0) {
          booksList.innerHTML =
            '<tr><td colspan="3">No borrowed books</td></tr>';
        } else {
          booksList.innerHTML = borrowedBooks
            .map((book) => {
              const dueDate = new Date(book.dueDate).toLocaleDateString();
              return `
                <tr>
                  <td>${book.book.title}</td>
                  <td>${dueDate}</td>
                  <td>${book.fine} Rwf</td>
                </tr>
              `;
            })
            .join("");
        }
      }

      function updateOutstandingFines(fines) {
        document.getElementById("outstanding-fines").textContent = fines;
      }

      function requestMembership() {
        const drawer = document.getElementById("membership-request-drawer");
        drawer.classList.remove("hidden");
        drawer.classList.add("open");
      }

      function closeDrawer() {
        const drawer = document.getElementById("membership-request-drawer");
        drawer.classList.remove("open");
        setTimeout(() => drawer.classList.add("hidden"), 300);
      }

      document
        .getElementById("membership-request-form")
        .addEventListener("submit", function (event) {
          event.preventDefault();
          alert("Membership request submitted successfully.");
          closeDrawer();
        });
    </script>
  </body>
</html>
