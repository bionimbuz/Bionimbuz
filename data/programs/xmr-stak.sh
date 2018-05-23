#!/bin/bash
readonly ARQUIVO_SAIDA=ResultadosMinerador.log

date --rfc-3339=ns >> $ARQUIVO_SAIDA
/usr/bin/time -vpao $ARQUIVO_SAIDA ./xmr-stak --currency aeon -o pool.aeon.hashvault.pro:3333 -u WmteSzxeCqd2eft5SEqbbQNLxH5feTTZc6KUb3EDv4GCcSrXxh8SxBXPWHipctgrRjWnNMUuFS5BwKLkcTterk7s21qHnEbnf -p work:anders1232@mail.com $@
date --rfc-3339=ns >> $ARQUIVO_SAIDA
