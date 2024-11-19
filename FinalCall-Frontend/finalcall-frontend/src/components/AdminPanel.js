// src/components/AdminPanel.js

import React, { useEffect, useState, useContext } from 'react';
import { AuthContext } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { authFetch } from '../utils/authFetch';

const AdminPanel = () => {
  const { user } = useContext(AuthContext);
  const navigate = useNavigate();
  const [users, setUsers] = useState([]);
  const [items, setItems] = useState([]);
  const [error, setError] = useState('');
  const [editingUser, setEditingUser] = useState(null); // State for editing a user
  const [editingItem, setEditingItem] = useState(null); // State for editing an item
  const [selectedImages, setSelectedImages] = useState([]); // State for multiple photo uploads

  useEffect(() => {
    if (!user || user.username !== 'admin') {
      navigate('/');
    } else {
      fetchUsers();
      fetchItems();
    }
  }, [user, navigate]);

  const fetchUsers = async () => {
    try {
      const response = await authFetch('http://localhost:8081/api/user/all', {
        method: 'GET',
      });
      if (response.ok) {
        const data = await response.json();
        setUsers(data);
      } else {
        const errorMsg = await response.text();
        setError(errorMsg);
      }
    } catch (err) {
      setError('Failed to fetch users.');
      console.error('Fetch Users Error:', err);
    }
  };

  const fetchItems = async () => {
    try {
      const response = await authFetch('http://localhost:8082/api/items', {
        method: 'GET',
      });
      if (response.ok) {
        const data = await response.json();
        setItems(data);
      } else {
        const errorMsg = await response.text();
        setError(errorMsg);
      }
    } catch (err) {
      setError('Failed to fetch items.');
    }
  };

  const handleDeleteUser = async (userId) => {
    try {
      const response = await authFetch(`http://localhost:8081/api/user/${userId}`, {
        method: 'DELETE',
      });
      if (response.ok) {
        setUsers(users.filter((user) => user.id !== userId));
      } else {
        const errorMsg = await response.text();
        setError(errorMsg);
      }
    } catch (err) {
      setError('Failed to delete user.');
    }
  };

  const handleDeleteItem = async (itemId) => {
    try {
      const response = await authFetch(`http://localhost:8082/api/items/${itemId}`, {
        method: 'DELETE',
      });
      if (response.ok) {
        setItems(items.filter((item) => item.id !== itemId));
      } else {
        const errorMsg = await response.text();
        setError(errorMsg);
      }
    } catch (err) {
      setError('Failed to delete item.');
    }
  };

  const handleEditUser = (user) => {
    if (user.username !== 'admin') {
      setEditingUser(user);
    }
  };

  const handleEditItem = (item) => {
    setEditingItem(item);
    setSelectedImages([]); // Clear selected images when starting to edit
  };

  const handleUserChange = (e) => {
    const { name, value } = e.target;
    setEditingUser((prevUser) => ({
      ...prevUser,
      [name]: value,
    }));
  };

  const handleItemChange = (e) => {
    const { name, value } = e.target;
    setEditingItem((prevItem) => ({
      ...prevItem,
      [name]: value,
    }));
  };

  const handleCheckboxChange = (e) => {
    const { name, checked } = e.target;
    setEditingUser((prevUser) => ({
      ...prevUser,
      [name]: checked,
    }));
  };

  const handlePhotoChange = (e) => {
    setSelectedImages([...e.target.files]);
  };

  const handleSaveUser = async () => {
    try {
      const userDetails = { ...editingUser };
      delete userDetails.password; // Remove password from the general update request

      const response = await authFetch(`http://localhost:8081/api/user/${editingUser.id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(userDetails),
      });

      if (response.ok) {
        setUsers(users.map((user) => (user.id === editingUser.id ? editingUser : user)));
        setEditingUser(null);
      } else {
        const errorMsg = await response.text();
        setError(errorMsg);
      }
    } catch (err) {
      setError('Failed to update user.');
    }
  };

  const handleSaveItem = async () => {
    try {
      // Update item details
      const response = await authFetch(`http://localhost:8082/api/items/${editingItem.id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(editingItem),
      });

      if (response.ok) {
        setItems(items.map((item) => (item.id === editingItem.id ? editingItem : item)));

        // Upload photos if any selected
        if (selectedImages.length > 0) {
          const formData = new FormData();
          selectedImages.forEach((file) => formData.append('images', file)); // Use 'images' key

          const photoResponse = await authFetch(`http://localhost:8082/api/items/${editingItem.id}/upload-image`, {
            method: 'POST',
            body: formData,
          });

          if (!photoResponse.ok) {
            const errorMsg = await photoResponse.text();
            setError(`Failed to upload photo(s): ${errorMsg}`);
          } else {
            // Optionally, refresh the item data to include new images
            fetchItems();
          }
        }

        setEditingItem(null);
        setSelectedImages([]);
      } else {
        const errorMsg = await response.text();
        setError(errorMsg);
      }
    } catch (err) {
      setError('Failed to update item.');
    }
  };

  const handleDeletePhoto = async (itemId, photoId) => {
    try {
      const response = await authFetch(`http://localhost:8082/api/items/${itemId}/photo/${photoId}`, {
        method: 'DELETE',
      });

      if (response.ok) {
        fetchItems(); // Refresh items list after delete
      } else {
        const errorMsg = await response.text();
        setError(`Failed to delete photo: ${errorMsg}`);
      }
    } catch (err) {
      setError('Failed to delete photo.');
    }
  };

  const handleCancelEdit = () => {
    setEditingUser(null);
    setEditingItem(null);
  };

  return (
    <div className="container mx-auto px-4 py-6">
      <h1 className="text-3xl font-bold mb-4">Admin Panel</h1>
      {error && (
        <div
          className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4"
          role="alert"
        >
          <strong className="font-bold">Error:</strong>
          <span className="block sm:inline"> {error}</span>
        </div>
      )}

      {/* Manage Users Section */}
      <div className="mb-8 overflow-y-auto max-h-80">
        <h2 className="text-2xl font-semibold mb-4">Manage Users</h2>
        {users.length === 0 ? (
          <p>No users available.</p>
        ) : (
          <table className="min-w-full bg-white border">
            <thead>
              <tr>
                <th className="py-2 px-4 border">Username</th>
                <th className="py-2 px-4 border">Email</th>
                <th className="py-2 px-4 border">Actions</th>
              </tr>
            </thead>
            <tbody>
              {users.map((user) => (
                <tr key={user.id} className="text-center">
                  <td className="py-2 px-4 border">{user.username}</td>
                  <td className="py-2 px-4 border">{user.email}</td>
                  <td className="py-2 px-4 border">
                    {user.username !== 'admin' && (
                      <>
                        <button
                          onClick={() => handleEditUser(user)}
                          className="bg-blue-500 text-white px-4 py-1 rounded hover:bg-blue-600 mr-2"
                        >
                          Edit
                        </button>
                        <button
                          onClick={() => handleDeleteUser(user.id)}
                          className="bg-red-500 text-white px-4 py-1 rounded hover:bg-red-600"
                        >
                          Delete
                        </button>
                      </>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Edit User Modal */}
      {editingUser && (
        <div className="mb-8">
          <h2 className="text-2xl font-semibold mb-4">Edit User</h2>
          <form className="bg-white p-6 rounded shadow-md grid grid-cols-2 gap-6">
            <div>
              <label className="block text-gray-700" htmlFor="username">
                Username
              </label>
              <input
                type="text"
                name="username"
                id="username"
                value={editingUser.username}
                onChange={handleUserChange}
                className="w-full px-3 py-2 border rounded mt-1"
                disabled
              />
            </div>
            <div>
              <label className="block text-gray-700" htmlFor="email">
                Email
              </label>
              <input
                type="email"
                name="email"
                id="email"
                value={editingUser.email}
                onChange={handleUserChange}
                className="w-full px-3 py-2 border rounded mt-1"
              />
            </div>
            <div className="col-span-2 flex gap-6">
              <div className="w-1/2">
                <label className="block text-gray-700" htmlFor="firstName">
                  First Name
                </label>
                <input
                  type="text"
                  name="firstName"
                  id="firstName"
                  value={editingUser.firstName}
                  onChange={handleUserChange}
                  className="w-full px-3 py-2 border rounded mt-1"
                />
              </div>
              <div className="w-1/2">
                <label className="block text-gray-700" htmlFor="lastName">
                  Last Name
                </label>
                <input
                  type="text"
                  name="lastName"
                  id="lastName"
                  value={editingUser.lastName}
                  onChange={handleUserChange}
                  className="w-full px-3 py-2 border rounded mt-1"
                />
              </div>
            </div>
            <div className="flex items-center gap-2">
              <input
                type="checkbox"
                name="isSeller"
                id="isSeller"
                checked={editingUser.isSeller}
                onChange={handleCheckboxChange}
                className="w-4 h-4"
              />
              <label htmlFor="isSeller" className="text-gray-700">
                Is Seller
              </label>
            </div>
            <div className="col-span-2 flex space-x-4 mt-4">
              <button
                type="button"
                onClick={handleSaveUser}
                className="bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600"
              >
                Save
              </button>
              <button
                type="button"
                onClick={handleCancelEdit}
                className="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-600"
              >
                Cancel
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Manage Items Section */}
      <div className="mb-8 overflow-y-auto max-h-80">
        <h2 className="text-2xl font-semibold mb-4">Manage Items</h2>
        {items.length === 0 ? (
          <p>No items available.</p>
        ) : (
          <table className="min-w-full bg-white border">
            <thead>
              <tr>
                <th className="py-2 px-4 border">Item Name</th>
                <th className="py-2 px-4 border">Current Bid ($)</th>
                <th className="py-2 px-4 border">Actions</th>
              </tr>
            </thead>
            <tbody>
              {items.map((item) => (
                <tr key={item.id} className="text-center">
                  <td className="py-2 px-4 border">{item.name}</td>
                  <td className="py-2 px-4 border">{item.currentBid.toFixed(2)}</td>
                  <td className="py-2 px-4 border">
                    <button
                      onClick={() => handleEditItem(item)}
                      className="bg-blue-500 text-white px-4 py-1 rounded hover:bg-blue-600 mr-2"
                    >
                      Edit
                    </button>
                    <button
                      onClick={() => handleDeleteItem(item.id)}
                      className="bg-red-500 text-white px-4 py-1 rounded hover:bg-red-600"
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Edit Item Modal */}
      {editingItem && (
        <div className="mb-8">
          <h2 className="text-2xl font-semibold mb-4">Edit Item</h2>
          <form className="bg-white p-6 rounded shadow-md grid grid-cols-2 gap-6">
            <div>
              <label className="block text-gray-700" htmlFor="name">
                Item Name
              </label>
              <input
                type="text"
                name="name"
                id="name"
                value={editingItem.name}
                onChange={handleItemChange}
                className="w-full px-3 py-2 border rounded mt-1"
              />
            </div>
            <div>
              <label className="block text-gray-700" htmlFor="currentBid">
                Current Bid ($)
              </label>
              <input
                type="number"
                name="currentBid"
                id="currentBid"
                value={editingItem.currentBid}
                onChange={handleItemChange}
                className="w-full px-3 py-2 border rounded mt-1"
              />
            </div>
            <div className="col-span-2">
              <label className="block text-gray-700" htmlFor="photos">
                Upload Photos
              </label>
              <input
                type="file"
                name="photos"
                id="photos"
                accept="image/*"
                multiple
                onChange={handlePhotoChange}
                className="w-full px-3 py-2 border rounded mt-1"
              />
              {selectedImages.length > 0 && (
                <div className="mt-2">
                  {selectedImages.map((image, index) => (
                    <p key={index} className="text-sm text-gray-600">{image.name}</p>
                  ))}
                </div>
              )}
            </div>
            <div className="col-span-2">
              {selectedImages.length > 0 && (
                <div className="carousel flex overflow-x-auto space-x-4 mt-4">
                  {selectedImages.map((image, index) => (
                    <div key={index} className="w-32 h-32 border rounded overflow-hidden">
                      <img
                        src={URL.createObjectURL(image)}
                        alt={`Preview ${index}`}
                        className="w-full h-full object-cover"
                      />
                    </div>
                  ))}
                </div>
              )}
            </div>
            <div className="col-span-2 flex space-x-4 mt-4">
              <button
                type="button"
                onClick={handleSaveItem}
                className="bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600"
              >
                Save
              </button>
              <button
                type="button"
                onClick={handleCancelEdit}
                className="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-600"
              >
                Cancel
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
};

export default AdminPanel;
