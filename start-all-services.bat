@echo off
echo Starting E-commerce Microservices...
echo =====================================

:: Start Eureka Service first
echo [%TIME%] Starting Eureka Service...
start "Eureka Service" cmd /k "cd /d backend\eureka-service && mvn spring-boot:run"

:: Wait 5 seconds before starting Gateway Service
echo [%TIME%] Waiting 5 seconds before starting Gateway Service...
timeout /t 5 /nobreak >nul

:: Start Gateway Service
echo [%TIME%] Starting Gateway Service...
start "Gateway Service" cmd /k "cd /d backend\gateway-service && mvn spring-boot:run"

:: Wait 5 seconds before starting remaining services
echo [%TIME%] Waiting 5 seconds before starting remaining services...
timeout /t 5 /nobreak >nul

:: Start Analytics Service
echo [%TIME%] Starting Analytics Service...
start "Analytics Service" cmd /k "cd /d backend\analytics-service && mvn spring-boot:run"

:: Wait 5 seconds
echo [%TIME%] Waiting 5 seconds...
timeout /t 5 /nobreak >nul

:: Start Order Service
echo [%TIME%] Starting Order Service...
start "Order Service" cmd /k "cd /d backend\order-service && mvn spring-boot:run"

:: Wait 5 seconds
echo [%TIME%] Waiting 5 seconds...
timeout /t 5 /nobreak >nul

:: Start Product Service
echo [%TIME%] Starting Product Service...
start "Product Service" cmd /k "cd /d backend\product-service && mvn spring-boot:run"

:: Wait 5 seconds
echo [%TIME%] Waiting 5 seconds...
timeout /t 5 /nobreak >nul

:: Start User Service
echo [%TIME%] Starting User Service...
start "User Service" cmd /k "cd /d backend\user-service && mvn spring-boot:run"

:: Wait 5 seconds
echo [%TIME%] Waiting 5 seconds...
timeout /t 5 /nobreak >nul

:: Start Wishlist Service
echo [%TIME%] Starting Wishlist Service...
start "Wishlist Service" cmd /k "cd /d backend\wishlist-service && mvn spring-boot:run"

:: Wait 5 seconds before starting frontend
echo [%TIME%] Waiting 5 seconds before starting frontend...
timeout /t 5 /nobreak >nul

:: Start Frontend Angular Application
echo [%TIME%] Starting Frontend Angular Application...
start "Frontend Angular App" cmd /k "cd frontend && npm start"

echo.
echo [%TIME%] All microservices and frontend have been started!
echo =========================================================
echo Services started in order:
echo 1. Eureka Service (Port: 8761)
echo 2. Gateway Service (5s delay)
echo 3. Analytics Service (5s delay)
echo 4. Order Service (5s delay)
echo 5. Product Service (5s delay)
echo 6. User Service (5s delay)
echo 7. Wishlist Service (5s delay)
echo 8. Frontend Angular App (5s delay - Port: 4200)
echo.
echo Each service is running in its own command window.
echo Close individual windows to stop specific services.
echo Frontend will be available at: http://localhost:4200
echo.
pause
