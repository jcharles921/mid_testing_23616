// Initial data and role
const userRole = "<%= role %>";

// Tab functionality
function showTab(tabId) {
  // Hide all tab contents
  document.querySelectorAll(".tab-content").forEach((tab) => {
    tab.classList.remove("active");
  });

  // Show selected tab content
  document.getElementById(tabId).classList.add("active");

  // If showing membership tab, fetch membership data
  if (tabId === "membership-management") {
    fetchMembershipRequests();
  }
}

// Fetch and display books
function fetchBooks() {
  fetch("books")
    .then((response) => response.json())
    .then((books) => {
      const tbody = document.getElementById("books-table-body");
      if (!books || books.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6">No books available</td></tr>';
        return;
      }

      tbody.innerHTML = books
        ?.map(
          (book) => `
            <tr>
              <td>${book.title}</td>
              <td>${book.edition}</td>
              <td>${book.ISBNCode}</td>
              <td>${book.publisherName}</td>
              <td>${book.bookStatus}</td>
              <td>
                <button onclick="deleteBook('${book.bookId}')" class="action-btn delete-btn">Delete</button>
              </td>
            </tr>
          `
        )
        .join("");
    })
    .catch((error) => {
      console.error("Error fetching books:", error);
      document.getElementById("books-table-body").innerHTML =
        '<tr><td colspan="6">Error loading books</td></tr>';
    });
}

// Fetch and display membership requests
function fetchMembershipRequests() {
  fetch("membership-requests")
    .then((response) => response.json())
    .then((requests) => {
      const tbody = document.getElementById("membership-table-body");
      if (!requests || requests.length === 0) {
        tbody.innerHTML =
          '<tr><td colspan="5">No pending membership requests</td></tr>';
        return;
      }

      tbody.innerHTML = requests
        .map(
          (request) => `
            <tr>
              <td>${request.name}</td>
              <td>${request.email}</td>
              <td>${new Date(request.requestDate).toLocaleDateString()}</td>
              <td>${request.status}</td>
              <td>
                <button onclick="handleMembershipRequest('${
                  request.id
                }', 'accept')" 
                        class="action-btn accept-btn">Accept</button>
                <button onclick="handleMembershipRequest('${
                  request.id
                }', 'refuse')" 
                        class="action-btn refuse-btn">Refuse</button>
              </td>
            </tr>
          `
        )
        .join("");
    })
    .catch((error) => {
      console.error("Error fetching membership requests:", error);
      document.getElementById("membership-table-body").innerHTML =
        '<tr><td colspan="5">Error loading membership requests</td></tr>';
    });
}

// Handle membership request (accept/refuse)
function handleMembershipRequest(requestId, action) {
  fetch("membership-requests", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      requestId,
      action,
      role: userRole,
    }),
  })
    .then((response) => response.json())
    .then((data) => {
      if (data.error) {
        alert(data.error);
      } else {
        alert(`Membership request ${action}ed successfully`);
        fetchMembershipRequests();
      }
    })
    .catch((error) => {
      console.error(`Error ${action}ing membership request:`, error);
      alert(`Error ${action}ing membership request`);
    });
}

// Existing functions
function addBook(bookData) {
  fetch("books", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      ...bookData,
      role: userRole,
    }),
  })
    .then((response) => response.json())
    .then((data) => {
      if (data.error) {
        alert(data.error);
      } else {
        alert("Book added successfully");
        closeDrawer();
        fetchBooks();
      }
    })
    .catch((error) => {
      console.error("Error adding book:", error);
      alert("Error adding book");
    });
}

function deleteBook(bookId) {
  if (!confirm("Are you sure you want to delete this book?")) return;

  fetch("books", {
    method: "DELETE",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      bookId: bookId,
      role: userRole,
    }),
  })
    .then((response) => response.json())
    .then((data) => {
      if (data.error) {
        alert(data.error);
      } else {
        alert("Book deleted successfully");
        fetchBooks();
      }
    })
    .catch((error) => {
      console.error("Error deleting book:", error);
      alert("Error deleting book");
    });
}

function openAddBookDrawer() {
  const overlay = document.getElementById("drawer-overlay");
  const drawer = document.getElementById("add-book-drawer");
  overlay.classList.add("visible");
  drawer.classList.remove("hidden");
  drawer.classList.add("open");
}

function closeDrawer() {
  const overlay = document.getElementById("drawer-overlay");
  const drawer = document.getElementById("add-book-drawer");
  overlay.classList.remove("visible");
  drawer.classList.remove("open");
  setTimeout(() => drawer.classList.add("hidden"), 300);
}

// Event Listeners
document.addEventListener("DOMContentLoaded", () => {
  const overlay = document.getElementById("drawer-overlay");
  const form = document.getElementById("add-book-form");

  overlay.addEventListener("click", closeDrawer);

  form.addEventListener("submit", (event) => {
    event.preventDefault();
    const formData = {
      title: form.title.value,
      edition: form.edition.value,
      ISBNCode: form.ISBNCode.value,
      publisherName: form.publisherName.value,
      publicationYear: form.publicationYear.value,
    };
    addBook(formData);
  });

  // Initial books fetch
  fetchBooks();
});
// Fetch and display shelves
function fetchShelves() {
  fetch("shelves")
    .then((response) => response.json())
    .then((shelves) => {
      const tbody = document.getElementById("shelf-table-body");
      if (!shelves || shelves.length === 0) {
        tbody.innerHTML = '<tr><td colspan="3">No shelves available</td></tr>';
        return;
      }

      tbody.innerHTML = shelves
        .map(
          (shelf) => `
    <tr>
      <td>${shelf.name}</td>
      <td>${shelf.roomName || "Unassigned"}</td>
      <td>
        <button onclick="openAssignRoomDrawer('${
          shelf.id
        }')" class="action-btn accept-btn">Assign Room</button>
        <button onclick="deleteShelf('${
          shelf.id
        }')" class="action-btn delete-btn">Delete</button>
      </td>
    </tr>
  `
        )
        .join("");
    })
    .catch((error) => {
      console.error("Error fetching shelves:", error);
      document.getElementById("shelf-table-body").innerHTML =
        '<tr><td colspan="3">Error loading shelves</td></tr>';
    });
}

// Open assign room drawer
function openAssignRoomDrawer(shelfId) {
  document.getElementById("assign-room-form").onsubmit = (event) => {
    event.preventDefault();
    assignRoomToShelf(shelfId, event.target.roomId.value);
  };

  populateRoomSelect();
  openDrawer("assign-room-drawer");
}

// Populate room select options
function populateRoomSelect() {
  fetch("rooms")
    .then((response) => response.json())
    .then((rooms) => {
      const select = document.getElementById("roomSelect");
      select.innerHTML = rooms
        .map((room) => `<option value="${room.id}">${room.name}</option>`)
        .join("");
    })
    .catch((error) => {
      console.error("Error fetching rooms:", error);
    });
}

// Assign room to shelf
function assignRoomToShelf(shelfId, roomId) {
  fetch("assign-room", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ shelfId, roomId }),
  })
    .then((response) => response.json())
    .then((data) => {
      if (data.error) {
        alert(data.error);
      } else {
        alert("Room assigned successfully");
        closeDrawer();
        fetchShelves();
      }
    })
    .catch((error) => {
      console.error("Error assigning room:", error);
    });
}

// Open add room drawer
function openAddRoomDrawer() {
  document.getElementById("add-room-form").onsubmit = (event) => {
    event.preventDefault();
    const formData = {
      roomCode: event.target.roomName.value,
    };
    addRoom(formData);
  };

  openDrawer("add-room-drawer");
}

// Add room
function addRoom(roomData) {
  fetch("rooms", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(roomData),
  })
    .then((response) => response.json())
    .then((data) => {
      if (data.error) {
        alert(data.error);
      } else {
        alert("Room added successfully");
        closeDrawer();
      }
    })
    .catch((error) => {
      console.error("Error adding room:", error);
    });
}

// Utility to open a specific drawer
function openDrawer(drawerId) {
  document.getElementById("drawer-overlay").classList.add("visible");
  document.getElementById(drawerId).classList.remove("hidden");
  document.getElementById(drawerId).classList.add("open");
}

// Close drawer
function closeDrawer() {
  document.getElementById("drawer-overlay").classList.remove("visible");
  document.querySelectorAll(".drawer").forEach((drawer) => {
    drawer.classList.remove("open");
    setTimeout(() => drawer.classList.add("hidden"), 300);
  });
}
let currentRoomId = null; // Track the room being edited
let shelves = []; // Store shelves temporarily

function openAddShelfDrawer() {
  openDrawer("add-shelf-drawer");
  fetchRooms();
}

document
  .getElementById("add-shelf-form")
  .addEventListener("submit", (event) => {
    event.preventDefault();

    const shelfData = {
      shelfName: event.target.shelfName.value,
      bookCategory: event.target.bookCategory.value,
      initialStock: parseInt(event.target.initialStock.value, 10),
      borrowedNumber: parseInt(event.target.borrowedNumber.value, 10),
      availableStock: parseInt(event.target.availableStock.value, 10),
      roomId: event.target.roomId.value,
    };

    addShelf(shelfData);
  });

function addShelf(shelfData) {
  fetch("shelves", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(shelfData),
  })
    .then((response) => response.json())
    .then((shelf) => {
      if (shelf.error) {
        alert(shelf.error);
        return;
      }

      shelves.push(shelf);
      updateShelfList();
      closeDrawer();
    })
    .catch((error) => {
      console.error("Error adding shelf:", error);
    });
}

function updateShelfList() {
  const shelfList = document.getElementById("shelf-list");
  if (shelves.length === 0) {
    shelfList.innerHTML = "<li>No shelves added yet</li>";
  } else {
    shelfList.innerHTML = shelves
      .map(
        (shelf) => `
        <li>
          ${shelf.shelfName} (Capacity: ${shelf.shelfCapacity})
          <button onclick="deleteShelf('${shelf.id}')">Delete</button>
        </li>`
      )
      .join("");
  }
}

function deleteShelf(shelfId) {
  if (!confirm("Are you sure you want to delete this shelf?")) return;

  fetch(`shelves?shelfId=${shelfId}`, { method: "DELETE" })
    .then((response) => response.json())
    .then((data) => {
      if (data.error) {
        alert(data.error);
        return;
      }

      shelves = shelves.filter((shelf) => shelf.id !== shelfId);
      updateShelfList();
    })
    .catch((error) => {
      console.error("Error deleting shelf:", error);
    });
}

function openEditRoomDrawer(roomId, roomName, roomCapacity) {
  currentRoomId = roomId;
  shelves = []; // Reset shelves when editing
  fetchShelvesForRoom(roomId);

  document.getElementById("drawer-title").textContent = "Edit Room";
  document.getElementById("roomName").value = roomName;
  document.getElementById("roomCapacity").value = roomCapacity;

  const form = document.getElementById("add-room-form");
  form.onsubmit = (event) => {
    event.preventDefault();
    const formData = {
      id: roomId,
      roomName: form.roomName.value,
      roomCapacity: form.roomCapacity.value,
    };
    updateRoom(formData);
  };

  openDrawer("add-room-drawer");
}

function fetchShelvesForRoom(roomId) {
  fetch(`shelves?roomId=${roomId}`)
    .then((response) => response.json())
    .then((data) => {
      shelves = data;
      updateShelfList();
    })
    .catch((error) => {
      console.error("Error fetching shelves:", error);
    });
}
function fetchRooms() {
  fetch("rooms")
    .then((response) => response.json())
    .then((rooms) => {
      const roomSelect = document.getElementById("roomId");
      console.log(rooms)
      rooms.forEach((room) => {
        const option = document.createElement("option");
        option.value = room.roomId; 
        option.textContent = `${room.roomCode}`; 
        roomSelect.appendChild(option);
      });
    })
    .catch((error) => console.error("Failed to fetch rooms:", error));
}

// Fetch shelves on page load
document.addEventListener("DOMContentLoaded", fetchShelves);
