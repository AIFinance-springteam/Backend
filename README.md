# AI Finance Backend

## 로컬 MySQL 실행

요구 사항은 Docker Desktop 또는 Docker Engine과 Docker Compose입니다.

1. 환경변수 예시 파일을 복사합니다.

   ```bash
   cp .env.example .env
   ```

2. `.env`의 빈 값을 로컬 전용 값으로 채웁니다.

   - `DB_PASSWORD`와 `MYSQL_PASSWORD`는 반드시 같은 값을 사용합니다.
   - `MYSQL_ROOT_PASSWORD`는 일반 사용자 비밀번호와 다른 값을 권장합니다.
   - `JWT_SECRET`도 로컬 전용 임의 문자열로 설정합니다.
   - 실제 OCR 테스트 전에는 CLOVA 관련 값은 비워 둘 수 있습니다.

3. MySQL을 실행하고 health 상태를 확인합니다.

   ```bash
   docker compose up -d mysql
   docker compose ps
   ```

4. Backend를 실행합니다.

   ```bash
   set -a
   source .env
   set +a
   ./gradlew bootRun
   ```

5. 컨테이너만 중지할 때는 다음 명령을 사용합니다. 데이터는 named volume에 유지됩니다.

   ```bash
   docker compose down
   ```

로컬 데이터를 포함해 초기화하려는 경우에만 `docker compose down -v`를 사용합니다.
