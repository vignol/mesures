#!/usr/bin/env bash
# Log CPU global (%) toutes N secondes dans un CSV : timestamp;cpu_percent
# Usage: ./cpu_logger.sh [fichier_csv] [periode_s]
set -euo pipefail
LC_ALL=C

OUTFILE="${1:-cpu_log.csv}"
PERIOD="${2:-5}"

# Ecrit l'entête si fichier absent ou vide
[ -s "$OUTFILE" ] || echo "timestamp;cpu_percent" >> "$OUTFILE"

read_cpu_idle_total() {
  # Lit la ligne 'cpu ' de /proc/stat et calcule idle & total
  awk '/^cpu /{
    u=$2;n=$3;s=$4;i=$5;w=$6;irq=$7;si=$8;st=$9;
    idle=i+w; nonidle=u+n+s+irq+si+st; total=idle+nonidle;
    print idle, total;
    exit
  }' /proc/stat
}

read IDLE1 TOTAL1 < <(read_cpu_idle_total)

cleanup() { echo "# stop" >&2; exit 0; }
trap cleanup INT TERM

while :; do
  sleep "$PERIOD"
  read IDLE2 TOTAL2 < <(read_cpu_idle_total)
  TOTALD=$(( TOTAL2 - TOTAL1 ))
  IDLED=$(( IDLE2 - IDLE1 ))

  if [ "$TOTALD" -gt 0 ]; then
    USAGE=$(awk -v td="$TOTALD" -v id="$IDLED" 'BEGIN{ printf("%.2f", (td-id)*100.0/td) }')
    TS=$(date -Iseconds)  # équiv. ISO-8601
    echo "$TS;$USAGE" >> "$OUTFILE"
  fi

  IDLE1=$IDLE2; TOTAL1=$TOTAL2
done

