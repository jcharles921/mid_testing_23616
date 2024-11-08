<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    // Directly access session attributes since authentication is handled by a filter
    String userName = (String) session.getAttribute("userName");
    String role = (String) session.getAttribute("role");

    // Validate role
    if (session == null || userName == null || role == null || !role.equals("LIBRARIAN")) {
        response.sendRedirect("index.html");
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Librarian Dashboard</title>
    <link rel="stylesheet" href="./styles/librarian.css" />
  </head>
  <body>
    <div class="dashboard">
      <!-- Sidebar -->
      <aside class="sidebar">
        <h2>Library Admin</h2>
        <nav>
          <ul>
            <li><a href="#book-management">Book Management</a></li>
            <li><a href="#membership-management">Membership Management</a></li>
            <li><a href="#fine-management">Fine Management</a></li>
            <li><a href="#reports">Reports & Analytics</a></li>
          </ul>
        </nav>
      </aside>

      <!-- Main Panel -->
      <main class="main-panel">
        <header>
          <h1>Welcome, <%= userName %>!</h1>
          <p>Role: Librarian</p>
        </header>

        <section class="summary-cards">
          <div class="card">
            <h3>Book Status</h3>
            <div class="chart-placeholder">Pie Chart Placeholder</div>
          </div>
          <div class="card">
            <h3>Overdue Books</h3>
            <ul>
              <li>Book 1 - Borrower: John Doe - Fine: $5</li>
              <li>Book 2 - Borrower: Jane Smith - Fine: $10</li>
            </ul>
          </div>
          <div class="card">
            <h3>Membership Requests</h3>
            <table>
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Membership Type</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>John Doe</td>
                  <td>Gold</td>
                  <td><button>Approve</button> <button>Reject</button></td>
                </tr>
                <tr>
                  <td>Jane Smith</td>
                  <td>Silver</td>
                  <td><button>Approve</button> <button>Reject</button></td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </main>
    </div>
  </body>
</html>
