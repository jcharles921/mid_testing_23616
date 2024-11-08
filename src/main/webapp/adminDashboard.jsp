<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    // Directly access session attributes since authentication is handled by a filter.
    String userName = (String) session.getAttribute("userName");
    String role = (String) session.getAttribute("role");
%>
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
            <li><a href="#borrowing-trends">Borrowing Trends</a></li>
            <li><a href="#membership-insights">Membership Insights</a></li>
            <li><a href="#location-data">Location Data</a></li>
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

          <div class="card" id="location-data">
            <h3>Location Breakdown</h3>
            <div class="chart-placeholder">Bar Chart Placeholder</div>
          </div>
        </section>
      </main>
    </div>
  </body>
</html>
