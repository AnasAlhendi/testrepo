const { app, BrowserWindow } = require('electron');

function createWindow() {
  const url = process.env.YOURAPP_URL || 'http://127.0.0.1:18080/';
  const win = new BrowserWindow({ width: 1280, height: 800 });
  win.loadURL(url);
}

app.whenReady().then(() => {
  createWindow();
  app.on('activate', function () {
    if (BrowserWindow.getAllWindows().length === 0) createWindow();
  });
});

app.on('window-all-closed', function () {
  if (process.platform !== 'darwin') app.quit();
});

