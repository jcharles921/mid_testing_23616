<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<% String userName = (String)
session.getAttribute("userName"); String role = (String)
session.getAttribute("role"); %>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<title>Librarian Dashboard</title>
<link rel="stylesheet" href="./styles/librarian.css" />
<style>
/* Existing styles */
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

#add-book-form {
	display: flex;
	flex-direction: column;
	gap: 10px;
}

#add-book-form input, #add-book-form button {
	padding: 8px;
	border-radius: 4px;
}

#add-book-form button {
	background-color: #77ba99;
	color: white;
	border: none;
	cursor: pointer;
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

/* New styles for tabs */
.tab-content {
	display: none;
}

.tab-content.active {
	display: block;
}

.action-btn {
	padding: 6px 12px;
	border-radius: 4px;
	border: none;
	cursor: pointer;
	margin: 0 4px;
}

.accept-btn {
	background-color: #4caf50;
	color: white;
}

.refuse-btn {
	background-color: #f44336;
	color: white;
}

.delete-btn {
	background-color: #f44336;
	color: white;
}
</style>
</head>
<body>
	<div class="dashboard">
		<!-- Sidebar -->
		<aside class="sidebar">
			<h2>Library Admin</h2>
			<nav>
				<ul>
					<li><a href="#" onclick="showTab('book-management')"
						class="tab-link">Book Management</a></li>
					<li><a href="#" onclick="showTab('membership-management')"
						class="tab-link">Membership Management</a></li>
					<li><a href="#" onclick="showTab('shelf-rooms')"
						class="tab-link">Shelfs and Rooms</a></li>
				</ul>
			</nav>
		</aside>

		<!-- Main Panel -->
		<main class="main-panel">
			<header>
				<h1>
					Welcome,
					<%= userName %>!
				</h1>
				<p>
					Role:
					<%= role %></p>
			</header>

			<!-- Books Section -->
			<div id="book-management" class="tab-content active">
				<div class="card">
					<div class="section-header">
						<h3>Available Books</h3>
						<button onclick="openAddBookDrawer()" class="add-book-btn">
							Add New Book</button>
					</div>
					<table>
						<thead>
							<tr>
								<th>Title</th>
								<th>Edition</th>
								<th>ISBN</th>
								<th>Publisher</th>
								<th>Status</th>
								<th>Actions</th>
							</tr>
						</thead>
						<tbody id="books-table-body">
							<tr>
								<td colspan="6">Loading books...</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<!-- Membership Management Section -->
			<div id="membership-management" class="tab-content">
				<div class="card">
					<div class="section-header">
						<h3>Membership Requests</h3>
					</div>
					<table>
						<thead>
							<tr>
								<th>Name</th>
								<th>Email</th>
								<th>Request Date</th>
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

			<!-- Shelfs and Rooms Section -->
			<div id="shelf-rooms" class="tab-content">
				<div class="card">
					<div class="section-header">
						<h3>Shelfs and Rooms</h3>

						<div style="display: flex; gap: 15px;">
							<button onclick="openAddRoomDrawer()" class="add-book-btn">
								Add Room</button>
							<button onclick="openAddShelfDrawer()" class="add-book-btn">Add
								Shelf</button>

						</div>
					</div>
					<table>
						<thead>
							<tr>
								<th>Shelf Name</th>
								<th>Assigned Room</th>
								<th>Actions</th>
							</tr>
						</thead>
						<tbody id="shelf-table-body">
							<tr>
								<td colspan="3">Loading shelves...</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>
		</main>
	</div>

	<!-- Add Book Drawer -->
	<div class="drawer-overlay" id="drawer-overlay"></div>
	<div id="add-book-drawer" class="drawer hidden">
		<div class="drawer-content">
			<button class="close-btn" onclick="closeDrawer()">×</button>
			<h3>Add New Book</h3>
			<form id="add-book-form">
				<input type="text" id="title" name="title" placeholder="Book Title"
					required /> <input type="number" id="edition"
					name="edition" placeholder="Edition" required /> <input
					type="text" id="ISBNCode" name="ISBNCode" placeholder="ISBN Code"
					required /> <input type="text" id="publisherName"
					name="publisherName" placeholder="Publisher Name" required />
				<input type="date" id="publicationYear" name="publicationYear"
					required />
				<button type="submit">Add Book</button>
			</form>
		</div>
	</div>
	<!-- Assign Room Drawer -->
	<div id="assign-room-drawer" class="drawer hidden">
		<div class="drawer-content">
			<button class="close-btn" onclick="closeDrawer()">×</button>
			<h3>Assign Room to Shelf</h3>
			<form id="assign-room-form">
				<select id="roomSelect" name="roomId" required>
					<option value="">Select Room</option>
				</select>
				<button type="submit">Assign Room</button>
			</form>
		</div>
	</div>

	<!-- Add Room Drawer -->
	<div id="add-room-drawer" class="drawer hidden">
		<div class="drawer-content">
			<button class="close-btn" onclick="closeDrawer()">×</button>
			<h3>Add New Room</h3>
			<form id="add-room-form">
				<input type="text" id="roomName" name="roomCode"
					placeholder="Room Name" required />

				<button type="submit">Add Room</button>
			</form>
		</div>
	</div>
  <!-- Add Shelf Drawer -->
<div id="add-shelf-drawer" class="drawer hidden">
  <div class="drawer-content">
    <button class="close-btn" onclick="closeDrawer()">×</button>
    <h3>Add Shelf</h3>
    <form id="add-shelf-form">
      <input
        type="text"
        id="shelfName"
        name="shelfName"
        placeholder="Shelf Name"
        required
      />
      <input
        type="text"
        id="bookCategory"
        name="bookCategory"
        placeholder="Book Category"
        required
      />
      <input
        type="number"
        id="initialStock"
        name="initialStock"
        placeholder="Initial Stock"
        required
      />
      <input
        type="number"
        id="borrowedNumber"
        name="borrowedNumber"
        placeholder="Borrowed Number"
        required
      />
      <input
        type="number"
        id="availableStock"
        name="availableStock"
        placeholder="Available Stock"
        required
      />
      <select id="roomId" name="roomId" required>
        <option value="" disabled selected>Select Room</option>
      </select>
      <button type="submit">Save Shelf</button>
    </form>
    
  </div>

	<script src="./scripts/library.js"></script>
</body>
</html>
