#!/bin/bash
readonly ARQUIVO_SAIDA=ResultadosMinerador.log
echo '------------------------------
Teste 10k hashes apenas em CPU
------------------------------'
for number in  {1..20}
do
date --rfc-3339=ns >> $ARQUIVO_SAIDA
/usr/bin/time -vpao $ARQUIVO_SAIDA ./xmr-stak --currency aeon -o pool.aeon.hashvault.pro:3333 -u WmteSzxeCqd2eft5SEqbbQNLxH5feTTZc6KUb3EDv4GCcSrXxh8SxBXPWHipctgrRjWnNMUuFS5BwKLkcTterk7s21qHnEbnf -p work:anders1232@mail.com --noAMD --noNVIDIA --hash-count 10000
date --rfc-3339=ns >> $ARQUIVO_SAIDA
done
echo '------------------------------
Teste 10k hashes com GPU e CPU
------------------------------'
for number in  {1..20}
do
date --rfc-3339=ns >> $ARQUIVO_SAIDA
/usr/bin/time -vpao $ARQUIVO_SAIDA ./xmr-stak --currency aeon -o pool.aeon.hashvault.pro:3333 -u WmteSzxeCqd2eft5SEqbbQNLxH5feTTZc6KUb3EDv4GCcSrXxh8SxBXPWHipctgrRjWnNMUuFS5BwKLkcTterk7s21qHnEbnf -p work:anders1232@mail.com --hash-count 10000
date --rfc-3339=ns >> $ARQUIVO_SAIDA
done
echo '------------------------------
Teste 100k hashes apenas em CPU
------------------------------'
for number in  {1..20}
do
date --rfc-3339=ns >> $ARQUIVO_SAIDA
/usr/bin/time -vpao $ARQUIVO_SAIDA ./xmr-stak --currency aeon -o pool.aeon.hashvault.pro:3333 -u WmteSzxeCqd2eft5SEqbbQNLxH5feTTZc6KUb3EDv4GCcSrXxh8SxBXPWHipctgrRjWnNMUuFS5BwKLkcTterk7s21qHnEbnf -p work:anders1232@mail.com --noAMD --noNVIDIA --hash-count 100000
date --rfc-3339=ns >> $ARQUIVO_SAIDA
done
echo '------------------------------
Teste 100k hashes com GPU e CPU
------------------------------'
for number in  {1..20}
do
date --rfc-3339=ns >> $ARQUIVO_SAIDA
/usr/bin/time -vpao $ARQUIVO_SAIDA ./xmr-stak --currency aeon -o pool.aeon.hashvault.pro:3333 -u WmteSzxeCqd2eft5SEqbbQNLxH5feTTZc6KUb3EDv4GCcSrXxh8SxBXPWHipctgrRjWnNMUuFS5BwKLkcTterk7s21qHnEbnf -p work:anders1232@mail.com --hash-count 100000
date --rfc-3339=ns >> $ARQUIVO_SAIDA
done
