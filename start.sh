#!/bin/bash

echo "🥬 우리마켓 실행 스크립트"
echo "========================"

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"

# 1. Maven Wrapper 설치 (없으면)
if [ ! -f "$PROJECT_DIR/backend/mvnw" ]; then
  echo ""
  echo "📦 Maven Wrapper 설치 중..."
  cd "$PROJECT_DIR/backend"
  mvn -N wrapper:wrapper -Dmaven=3.9.6 2>/dev/null || {
    echo "⚠️  mvn 명령어가 없습니다. Homebrew로 설치합니다..."
    brew install maven 2>/dev/null
    mvn -N wrapper:wrapper -Dmaven=3.9.6
  }
fi

# 2. 백엔드 실행
echo ""
echo "🚀 백엔드 서버 시작 중... (Spring Boot)"
cd "$PROJECT_DIR/backend"
if [ -f "./mvnw" ]; then
  ./mvnw spring-boot:run &
else
  mvn spring-boot:run &
fi
BACKEND_PID=$!
echo "   백엔드 PID: $BACKEND_PID"

# 백엔드가 시작될 때까지 대기
echo "   백엔드 시작 대기 중..."
for i in $(seq 1 30); do
  if curl -s http://localhost:8080/h2-console > /dev/null 2>&1; then
    echo "   ✅ 백엔드 시작 완료! (http://localhost:8080)"
    break
  fi
  sleep 2
done

# 3. 프론트엔드 실행
echo ""
echo "🎨 프론트엔드 시작 중... (Svelte + Vite)"
cd "$PROJECT_DIR/frontend"
if [ ! -d "node_modules" ]; then
  echo "   npm install 실행 중..."
  npm install
fi
npm run dev &
FRONTEND_PID=$!
echo "   프론트엔드 PID: $FRONTEND_PID"

sleep 3

echo ""
echo "========================================="
echo "🥬 우리마켓 실행 완료!"
echo "========================================="
echo ""
echo "🌐 프론트엔드:  http://localhost:5173"
echo "🔧 백엔드 API:  http://localhost:8080"
echo "🗃️  H2 콘솔:    http://localhost:8080/h2-console"
echo ""
echo "📌 테스트 빠른 로그인:"
echo "   - 판매자 (김판매): 01012345678"
echo "   - 구매자 (박구매): 01011112222"
echo "   - Mock 인증번호: 123456"
echo ""
echo "종료하려면 Ctrl+C 를 누르세요."
echo ""

# 종료 처리
trap "echo '종료 중...'; kill $BACKEND_PID $FRONTEND_PID 2>/dev/null; exit 0" SIGINT SIGTERM

wait
