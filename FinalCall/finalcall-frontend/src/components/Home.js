// src/components/Home.js
import React from 'react';
import { useNavigate } from 'react-router-dom';

const Home = () => {
    const navigate = useNavigate();

    return (
        <div className="flex flex-col items-center justify-center h-screen bg-gray-100">
            <h1 className="text-4xl font-bold mb-8">Welcome to FinalCall</h1>
            <div className="flex space-x-4">
                <button 
                    onClick={() => navigate('/login')}
                    className="px-4 py-2 bg-blue-500 text-white rounded"
                >
                    Login
                </button>
                <button 
                    onClick={() => navigate('/register')}
                    className="px-4 py-2 bg-green-500 text-white rounded"
                >
                    Register
                </button>
            </div>
        </div>
    );
}

export default Home;
