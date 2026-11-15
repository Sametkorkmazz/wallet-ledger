import {
  Card,
  CardAction,
  CardContent,
  CardHeader,
} from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";

export default function BalanceCardSkeleton() {
  return (
    <Card className="h-full [--card-spacing:--spacing(6)]">
      <CardHeader>
        <Skeleton className="h-5 w-24" />
        <Skeleton className="mt-2 h-9 w-40" />
        <CardAction>
          <Skeleton className="size-9 rounded-md" />
        </CardAction>
      </CardHeader>
      <CardContent className="flex flex-col gap-1">
        <Skeleton className="h-5 w-20" />
        <Skeleton className="h-5 w-48" />
      </CardContent>
    </Card>
  );
}
