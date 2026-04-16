import React, { useState } from 'react';
import './App.css';
import { MapContainer, TileLayer, CircleMarker, Popup } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';

const API_URL = 'http://localhost:8080/api/earthquakes';

function App() {
  const [earthquakes, setEarthquakes] = useState([]);
  const [loading, setLoading] = useState(false);
  const [minMag, setMinMag] = useState('');
  const [afterTime, setAfterTime] = useState('');

  const fetchEarthquakes = async () => {
    setLoading(true);
    try {
      await fetch(`${API_URL}/fetch`, { method: 'POST' });
      const response = await fetch(API_URL);
      const data = await response.json();
      setEarthquakes(data);
    } catch (error) {
      alert('Error fetching earthquakes: ' + error.message);
    }
    setLoading(false);
  };

  const filterByMagnitude = async () => {
    setLoading(true);
    try {
      const response = await fetch(`${API_URL}/filter?minMag=2.0`);
      const data = await response.json();
      setEarthquakes(data);
    } catch (error) {
      alert('Error filtering: ' + error.message);
    }
    setLoading(false);
  };

  const filterByTime = async () => {
    if (!afterTime) return;
    setLoading(true);
    try {
      const response = await fetch(`${API_URL}/after?time=${new Date(afterTime).toISOString()}`);
      const data = await response.json();
      setEarthquakes(data);
    } catch (error) {
      alert('Error filtering by time: ' + error.message);
    }
    setLoading(false);
  };

  const showAll = async () => {
    setLoading(true);
    try {
      const response = await fetch(API_URL);
      const data = await response.json();
      setEarthquakes(data);
    } catch (error) {
      alert('Error: ' + error.message);
    }
    setLoading(false);
  };

  return (
      <div className="container">
        <h1>Earthquake Tracker</h1>

        <div className="section">
          <button onClick={fetchEarthquakes} disabled={loading}>
            {loading ? 'Loading...' : ' Fetch Latest Earthquakes'}
          </button>
          <button onClick={showAll} disabled={loading}>
             Show All
          </button>
        </div>

        <div className="section">
          <button onClick={filterByMagnitude} disabled={loading}>
             Show Magnitude &gt; 2.0
          </button>
        </div>

        <div className="section">
          <h3>Filter by Time</h3>
          <input
              type="datetime-local"
              value={afterTime}
              onChange={(e) => setAfterTime(e.target.value)}
          />
          <button onClick={filterByTime}>Filter</button>
        </div>

        <p>Showing <strong>{earthquakes.length}</strong> earthquakes</p>

        <table>
          <thead>
          <tr>
            <th>Magnitude</th>
            <th>Mag Type</th>
            <th>Place</th>
            <th>Title</th>
            <th>Time</th>
          </tr>
          </thead>
          <tbody>
          {earthquakes.length === 0 ? (
              <tr>
                <td colSpan="5">No data yet. Click "Fetch Latest Earthquakes" to load data.</td>
              </tr>
          ) : (
              earthquakes.map((eq) => (
                  <tr key={eq.id} className={eq.magnitude >= 2.0 ? 'high-mag' : ''}>
                    <td>{eq.magnitude}</td>
                    <td>{eq.magType}</td>
                    <td>{eq.place}</td>
                    <td>{eq.title}</td>
                    <td>{new Date(eq.time).toLocaleString()}</td>
                  </tr>
              ))
          )}
          </tbody>
        </table>
        {earthquakes.length > 0 && (
            <div style={{ marginTop: '30px' }}>
              <h2>Earthquake Map</h2>
              <MapContainer
                  center={[20, 0]}
                  zoom={2}
                  style={{ height: '500px', width: '100%', borderRadius: '10px' }}
              >
                <TileLayer
                    url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                    attribution="© OpenStreetMap contributors"
                />
                {earthquakes.map((eq) =>
                    eq.latitude && eq.longitude ? (
                        <CircleMarker
                            key={eq.id}
                            center={[eq.latitude, eq.longitude]}
                            radius={eq.magnitude * 3}
                            color={eq.magnitude >= 2.0 ? 'red' : 'orange'}
                            fillOpacity={0.6}
                        >
                          <Popup>
                            <strong>{eq.title}</strong><br />
                            Magnitude: {eq.magnitude}<br />
                            Place: {eq.place}<br />
                            Time: {new Date(eq.time).toLocaleString()}
                          </Popup>
                        </CircleMarker>
                    ) : null
                )}
              </MapContainer>
            </div>
        )}
      </div>
  );
}

export default App;