#!/usr/bin/env python3

import argparse
import base64
import os
import sys
from pathlib import Path

import pg8000
from cryptography.hazmat.primitives.ciphers.aead import AESGCM
from dotenv import load_dotenv


ENCRYPTION_PREFIX = "ENC:"


def encrypt_password(plaintext: str, secret: str) -> str:
    if plaintext is None:
        return None

    key = secret[:32].encode("utf-8")
    iv = os.urandom(12)
    aesgcm = AESGCM(key)
    ciphertext = aesgcm.encrypt(iv, plaintext.encode("utf-8"), None)
    combined = iv + ciphertext
    return ENCRYPTION_PREFIX + base64.b64encode(combined).decode("utf-8")


def is_already_encrypted(password: str) -> bool:
    if not password:
        return False
    return password.startswith(ENCRYPTION_PREFIX)


def run_test_mode(secret: str):
    test_password = "TEST_PASSWORD_123"
    encrypted = encrypt_password(test_password, secret)
    print("=" * 60)
    print("TEST ENCRYPTION RESULTS")
    print("=" * 60)
    print(f"Original password: {test_password}")
    print(f"Encrypted (Base64): {encrypted}")
    print("=" * 60)
    print("\nCopy the encrypted value to EncryptionCompatibilityTest.java")
    print("and run: ./gradlew test --tests 'EncryptionCompatibilityTest'")
    print("=" * 60)


def run_migration(secret: str, db_config: dict, dry_run: bool = False):
    conn = None
    try:
        print("Connecting to database...")
        conn = pg8000.connect(
            host=db_config["host"],
            port=db_config["port"],
            database=db_config["database"],
            user=db_config["user"],
            password=db_config["password"],
        )
        cursor = conn.cursor()

        print("Fetching users with passwords...")
        cursor.execute("SELECT id, password FROM users WHERE password IS NOT NULL")
        users = cursor.fetchall()

        print(f"Found {len(users)} users with passwords")

        migrated = 0
        skipped = 0
        errors = 0

        for user_id, password in users:
            try:
                if is_already_encrypted(password):
                    print(f"  [SKIP] User {user_id}: password already encrypted")
                    skipped += 1
                    continue

                encrypted = encrypt_password(password, secret)

                if dry_run:
                    print(f"  [DRY-RUN] User {user_id}: would encrypt password")
                    migrated += 1
                else:
                    cursor.execute(
                        "UPDATE users SET password = %s WHERE id = %s",
                        (encrypted, user_id),
                    )
                    conn.commit()
                    print(f"  [OK] User {user_id}: password encrypted")
                    migrated += 1

            except Exception as e:
                print(f"  [ERROR] User {user_id}: {e}")
                errors += 1
                conn.rollback()

        print("\n" + "=" * 60)
        print("MIGRATION SUMMARY")
        print("=" * 60)
        print(f"Total users:    {len(users)}")
        print(f"Migrated:       {migrated}")
        print(f"Skipped:        {skipped}")
        print(f"Errors:         {errors}")
        if dry_run:
            print("Mode:           DRY-RUN (no changes made)")
        print("=" * 60)

    finally:
        if conn:
            conn.close()


def parse_db_url(db_url: str) -> dict:
    if db_url.startswith("jdbc:postgresql://"):
        db_url = db_url.replace("jdbc:postgresql://", "")

    parts = db_url.split("/")
    host_port = parts[0].split(":")
    database = parts[1] if len(parts) > 1 else ""

    return {
        "host": host_port[0],
        "port": int(host_port[1]) if len(host_port) > 1 else 5432,
        "database": database,
    }


def main():
    parser = argparse.ArgumentParser(
        description="Migrate user passwords to encrypted format"
    )
    parser.add_argument(
        "--test-only", action="store_true", help="Run test encryption only"
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Show what would be done without making changes",
    )
    parser.add_argument(
        "--env-file", default=".env", help="Path to .env file (default: .env)"
    )
    args = parser.parse_args()

    env_path = Path(args.env_file)
    if env_path.exists():
        print(f"Loading environment from {env_path}")
        load_dotenv(env_path)
    else:
        print(f"Warning: {env_path} not found, using system environment")

    secret = os.getenv("ENCRYPTION_SECRET_KEY")
    if not secret:
        print("ERROR: ENCRYPTION_SECRET_KEY not found in environment")
        sys.exit(1)

    if len(secret) < 32:
        print("ERROR: ENCRYPTION_SECRET_KEY must be at least 32 characters")
        sys.exit(1)

    if args.test_only:
        run_test_mode(secret)
        return

    db_url = os.getenv("DB_URL")
    db_name = os.getenv("DB_NAME")
    db_user = os.getenv("DB_USERNAME")
    db_password = os.getenv("DB_PASSWORD")

    if not all([db_url, db_user, db_password]):
        print("ERROR: Missing database configuration")
        print("Required: DB_URL, DB_USERNAME, DB_PASSWORD")
        sys.exit(1)

    db_config = parse_db_url(db_url)
    if db_name:
        db_config["database"] = db_name
    db_config["user"] = db_user
    db_config["password"] = db_password

    run_migration(secret, db_config, dry_run=args.dry_run)


if __name__ == "__main__":
    main()
