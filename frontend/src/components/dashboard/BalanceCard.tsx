import { CURRENCY_NAME, CURRENCY_SYMBOL, type Balance } from "@/api/types";
import {
  Card,
  CardAction,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { formatMoney } from "@/lib/money";

type BalanceCardProps = {
  balance: Balance;
};

export default function BalanceCard({ balance }: BalanceCardProps) {
  return (
    <Card className="[--card-spacing:--spacing(6)]">
      <CardHeader>
        <CardDescription>
          {CURRENCY_NAME[balance.currency]} balance
        </CardDescription>
        <CardTitle className="mt-2 text-3xl font-semibold tabular-nums">
          {formatMoney(balance.amount, balance.currency)}
        </CardTitle>
        <CardAction>
          <span className="rounded-md bg-primary/12 px-3.5 py-2 text-[16px] font-medium text-primary">
            {CURRENCY_SYMBOL[balance.currency]}
          </span>
        </CardAction>
      </CardHeader>
      <CardContent className="flex flex-wrap items-center gap-x-2 text-sm">
        <span className="font-medium text-success">
          +{formatMoney(balance.inThisMonth, balance.currency)} in
        </span>
        <span aria-hidden="true" className="text-muted-foreground">
          ·
        </span>
        <span className="text-muted-foreground">
          −{formatMoney(balance.outThisMonth, balance.currency)} out this month
        </span>
      </CardContent>
    </Card>
  );
}
