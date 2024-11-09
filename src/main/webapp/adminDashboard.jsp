<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%> <% String userName = (String)
session.getAttribute("userName"); String role = (String)
session.getAttribute("role"); %>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Administrative Dashboard</title>
    <link rel="stylesheet" href="./styles/administrative.css" />
  </head>
  <body>
    <div class="dashboard">
      <!-- Sidebar -->
      <aside class="sidebar">
        <h2>Admin Dashboard</h2>
        <nav>
          <ul>
            <li>
              <a href="#" class="tab-link" data-target="borrowing-trends"
                >Borrowing Trends</a
              >
            </li>
            <li>
              <a href="#" class="tab-link" data-target="membership-insights"
                >Membership Insights</a
              >
            </li>
            <li>
              <a href="#" class="tab-link" data-target="borrows-management"
                >Book Borrowing</a
              >
            </li>
            <li>
              <a href="#" class="tab-link" data-target="membership-management"
                >Membership Requests</a
              >
            </li>
          </ul>
        </nav>
      </aside>

      <!-- Main Panel -->
      <main class="main-panel">
        <header>
          <h1>Welcome, <%= userName %>!</h1>
          <p>Role: <%= role %></p>
        </header>

        <section class="summary-cards">
          <div class="card" id="borrowing-trends">
            <h3>Borrowing Trends</h3>
            <div class="chart-placeholder">Line Chart Placeholder</div>
          </div>

          <div class="card" id="membership-insights">
            <h3>Active Membership Summary</h3>
            <div class="membership-cards">
              <div class="membership-card">
                <h4>Gold</h4>
                <p>50 Members</p>
              </div>
              <div class="membership-card">
                <h4>Silver</h4>
                <p>75 Members</p>
              </div>
              <div class="membership-card">
                <h4>Striver</h4>
                <p>120 Members</p>
              </div>
            </div>
          </div>
        </section>
        <div id="borrows-management" class="tab-content">
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
        <div id="membership-management" class="tab-content">
          <div class="card">
            <div class="section-header">
              <h3>Membership Requests</h3>
            </div>
            <table>
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Membership type</th>
                  <th>Request Date</th>
                  <th>Expiration Date</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody id="membership-table-body">
                <tr>
                  <td colspan="5">Loading membership requests...</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </main>
    </div>
  </body>
  <script src="./scripts/administrators.js"></script>
</html>
