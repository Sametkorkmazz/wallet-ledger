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
        <div className="caption">
          <Skeleton className="h-lh w-24" />
        </div>
        <div className="mt-2 text-3xl font-semibold">
          <Skeleton className="h-lh w-40" />
        </div>
        <CardAction>
          <Skeleton className="size-9 rounded-md" />
        </CardAction>
      </CardHeader>
      <CardContent className="flex flex-col gap-1">
        <div className="text-sm">
          <Skeleton className="h-lh w-20" />
        </div>
        <div className="text-sm">
          <Skeleton className="h-lh w-48" />
        </div>
      </CardContent>
    </Card>
  );
}
